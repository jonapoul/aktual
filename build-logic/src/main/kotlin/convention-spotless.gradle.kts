plugins {
  id("com.diffplug.spotless")
}

spotless {
  format("misc") {
    target("*.gradle", "*.gitignore", "*.pro")
    indentWithSpaces()
    trimTrailingWhitespace()
    endWithNewline()
  }
  json {
    target("*.json")
    simple()
  }
  yaml {
    target("*.yml", "*.yaml")
    jackson()
  }
}
