"""Maintains one PR comment that collects CI results from several jobs.

Each job writes its own section as a file, uploaded as a `pr-comment-<section>` artifact. Rendering
downloads every section uploaded so far and rewrites the whole comment, so the final render from the
finish job always holds every section, even if two jobs raced earlier.

Commands:
  create              find or create the comment, print its ID
  section <file>      write this job's section to <file>
  render <dir>        rebuild the comment from the section files in <dir>
"""

import glob
import json
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

MARKER = "<!-- pr-report -->"
ORDER = ["status", "apk", "validation", "assemble-android", "assemble-desktop", "detekt", "lint", "tests", "other-checks"]
MAX_ITEMS = 15
MAX_BODY = 60000
MAX_LOG_LINES = 20
ANSI = re.compile(r"\x1b\[[0-9;]*[A-Za-z]")
DETEKT_ISSUE = re.compile(r"^file://(\S+):(\d+):\d+ \[(\w+)\] (.*)$")
RUNNING_MODULES = re.compile(r"^Running \S+ on \d+ modules?:$")
FAILED_TASKS = re.compile(r"^Failed task\(s\):$")
MODULES_REASON = re.compile(r"^(Forced - |Build/config files changed |Version catalog changed - )")
KOTLIN_ERROR = re.compile(r"^e: (?:file://)?(\S+?\.kts?):(\d+):\d+ (.*)$")

REPO = os.environ["GITHUB_REPOSITORY"]
SERVER = os.environ.get("GITHUB_SERVER_URL", "https://github.com")
RUN_URL = f"{SERVER}/{REPO}/actions/runs/{os.environ['GITHUB_RUN_ID']}"
WORKSPACE = os.environ.get("GITHUB_WORKSPACE", os.getcwd())


def gh(*args, data=None):
    out = subprocess.run(["gh", "api", *args], input=data, capture_output=True, text=True, check=True)
    return out.stdout


def create():
    pr = os.environ["PR"]
    ids = gh(f"repos/{REPO}/issues/{pr}/comments", "--paginate", "--jq", f'.[] | select(.body | startswith("{MARKER}")) | .id')
    existing = ids.split()
    if existing:
        print(existing[0])
        return
    body = json.dumps({"body": MARKER})
    print(gh(f"repos/{REPO}/issues/{pr}/comments", "--input", "-", "--jq", ".id", data=body).strip())


def job_url():
    attempt = os.environ.get("GITHUB_RUN_ATTEMPT", "1")
    try:
        jobs = gh(f"repos/{REPO}/actions/runs/{os.environ['GITHUB_RUN_ID']}/attempts/{attempt}/jobs", "--paginate", "--jq", ".jobs[] | [.name, .html_url] | @tsv")
    except subprocess.CalledProcessError:
        return RUN_URL
    for line in jobs.splitlines():
        name, url = line.split("\t", 1)
        if name == os.environ["GITHUB_JOB"]:
            return url
    return RUN_URL


def relative(path):
    if not path:
        return ""
    return os.path.relpath(os.path.abspath(path), WORKSPACE)


def file_link(path, line):
    path = relative(path)
    if not path:
        return "unknown file"
    sha = os.environ.get("SHA", "")
    suffix = f"#L{line}" if line else ""
    label = f"{path}:{line}" if line else path
    return f"[`{label}`]({SERVER}/{REPO}/blob/{sha}/{path}{suffix})"


def first_line(text):
    lines = (text or "").strip().splitlines()
    return escape(lines[0].strip()) if lines else ""


def escape(text):
    return (text or "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def parse_reports():
    tests, checks = [], []
    paths = glob.glob("**/build/test-results/**/*.xml", recursive=True)
    paths += glob.glob("**/build/reports/**/*.xml", recursive=True)
    for path in sorted(set(paths)):
        try:
            root = ET.parse(path).getroot()
        except (ET.ParseError, OSError):
            continue
        if root.tag in ("testsuite", "testsuites"):
            for case in root.iter("testcase"):
                problem = case.find("failure")
                if problem is None:
                    problem = case.find("error")
                if problem is not None:
                    message = first_line(problem.get("message") or problem.text)
                    tests.append(f"`{case.get('classname')}.{case.get('name')}`: {message}")
        elif root.tag == "issues":
            for issue in root.iter("issue"):
                if issue.get("severity") not in ("Error", "Fatal"):
                    continue
                location = issue.find("location")
                file = location.get("file") if location is not None else None
                if file and not os.path.isabs(file):
                    file = os.path.join(path.split("/build/")[0], file)
                line = location.get("line") if location is not None else None
                checks.append(f"{file_link(file, line)} {escape(issue.get('message'))} ({issue.get('id')})")
    checks += parse_detekt()
    return tests, checks


def parse_detekt():
    """Reads the merged issues written by DetektSummaryService"""
    try:
        with open(os.path.join(WORKSPACE, "build/reports/detekt/all-issues.txt")) as f:
            lines = f.read().splitlines()
    except OSError:
        return []
    issues = []
    for line in lines:
        if match := DETEKT_ISSUE.match(line):
            file, number, rule, message = match.groups()
            issues.append(f"{file_link(file, number)} {escape(message)} ({rule})")
    return issues


def parse_log():
    """Returns (errors, excerpt, modules) from the failing step's output, kept by run-step.sh"""
    path = os.path.join(os.environ.get("RUNNER_TEMP", ""), "failed-step.log")
    try:
        with open(path, errors="replace") as f:
            lines = [ANSI.sub("", line.rstrip()) for line in f]
    except OSError:
        return [], [], ""
    errors = {}
    for line in lines:
        if match := KOTLIN_ERROR.match(line):
            file, number, message = match.groups()
            errors[f"{file_link(file, number)} {escape(message)}"] = None
        elif line.startswith("e: "):
            errors[escape(line[3:])] = None
        elif line.startswith("::error"):
            errors[escape(line.split("::", 2)[-1])] = None
    excerpt, inside = [], False
    for line in lines:
        if line == "* What went wrong:":
            inside = True
        elif line.startswith("* ") or line.startswith("====="):
            inside = False
        elif inside and line.strip():
            excerpt.append(line)
    if not excerpt:
        excerpt = lines[-MAX_LOG_LINES:]
    return list(errors), excerpt[:MAX_LOG_LINES], modules_block(lines)


def indented_after(lines, header):
    """The indented lines straight after the first line matching header, as printed by modules.sh"""
    for i, line in enumerate(lines):
        if header.match(line):
            items = []
            for item in lines[i + 1:]:
                if not item.startswith("  "):
                    break
                items.append(item.strip())
            return line, items
    return None, []


def modules_block(lines):
    """Lists the modules scripts/lib/modules.sh ran the task on, marking the failed ones"""
    header, tasks = indented_after(lines, RUNNING_MODULES)
    if not header:
        return ""
    failed = {task.rsplit(":", 1)[0] for task in indented_after(lines, FAILED_TASKS)[1]}
    reason = next((line for line in lines if MODULES_REASON.match(line)), None)
    modules = [task.rsplit(":", 1)[0] for task in tasks]
    count = len(modules)
    items = [f":x: `{module}`" if module in failed else f"`{module}`" for module in modules]
    parts = [f"<details><summary>Ran on {count} module{'' if count == 1 else 's'}, {len(failed)} failed</summary>", ""]
    if reason:
        parts += [escape(reason), ""]
    parts += [f"- {item}" for item in items]
    parts += ["", "</details>"]
    return "\n".join(parts)


def code_block(lines):
    return "```text\n" + "\n".join(lines).replace("```", "` ` `") + "\n```"


def bullets(items):
    lines = [f"- {item}" for item in items[:MAX_ITEMS]]
    if len(items) > MAX_ITEMS:
        lines.append(f"- ...and {len(items) - MAX_ITEMS} more")
    return "\n".join(lines)


def section(out_path):
    body = os.environ.get("BODY", "").strip()
    status = os.environ.get("STATUS", "")
    title = os.environ.get("TITLE", "")
    if not body and status not in ("", "success"):
        tests, checks = parse_reports()
        errors, excerpt, modules = parse_log()
        parts = [f"#### :x: {title} ([logs]({job_url()}))"]
        if errors:
            parts.append(f"{len(errors)} error(s):\n{bullets(errors)}")
        if tests:
            parts.append(f"{len(tests)} failed test(s):\n{bullets(tests)}")
        if checks:
            parts.append(f"{len(checks)} issue(s):\n{bullets(checks)}")
        if not errors and not tests and not checks:
            parts.append(code_block(excerpt) if excerpt else "See the logs for details.")
        if modules:
            parts.append(modules)
        body = "\n\n".join(parts)
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w") as f:
        f.write(body + "\n")


def render(directory):
    sections = {}
    for path in glob.glob(os.path.join(directory, "*.md")):
        with open(path) as f:
            text = f.read().strip()
        if text:
            sections[os.path.basename(path).removesuffix(".md")] = text
    keys = sorted(sections, key=lambda k: (ORDER.index(k) if k in ORDER else len(ORDER), k))
    body = "\n\n".join([MARKER] + [sections[k] for k in keys])
    if len(body) > MAX_BODY:
        body = body[:MAX_BODY] + f"\n\n...truncated, see the [run]({RUN_URL})"
    gh("-X", "PATCH", f"repos/{REPO}/issues/comments/{os.environ['COMMENT_ID']}", "--input", "-", data=json.dumps({"body": body}))


if __name__ == "__main__":
    command = sys.argv[1]
    if command == "create":
        create()
    elif command == "section":
        section(sys.argv[2])
    elif command == "render":
        render(sys.argv[2])
    else:
        sys.exit(f"Unknown command {command}")
