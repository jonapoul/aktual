import java.io.File;
import java.nio.file.Files;
import java.util.*;
import kotlin.metadata.*;
import kotlin.metadata.jvm.*;
import org.objectweb.asm.*;

// Args: root dir, then ":module=rel/dir" pairs. Prints "module\texposedModule\texample".
public class Exposed {
  static final List<String> CLASS_DIRS =
      List.of("build/classes/kotlin/desktop/main", "build/classes/kotlin/android/main", "build/classes/kotlin/main");

  static class Collector extends ClassVisitor {
    String name;
    boolean found;
    Map<String, Object> values = new HashMap<>();

    Collector() {
      super(Opcodes.ASM9);
    }

    @Override
    public void visit(int v, int a, String n, String s, String sup, String[] i) {
      name = n;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      if (!desc.equals("Lkotlin/Metadata;")) return null;
      found = true;
      return new AnnotationVisitor(Opcodes.ASM9) {
        @Override
        public void visit(String n, Object value) {
          values.put(n, value);
        }

        @Override
        public AnnotationVisitor visitArray(String n) {
          List<Object> list = new ArrayList<>();
          values.put(n, list);
          return new AnnotationVisitor(Opcodes.ASM9) {
            @Override
            public void visit(String x, Object value) {
              list.add(value);
            }
          };
        }
      };
    }

    @SuppressWarnings("unchecked")
    kotlin.Metadata metadata() {
      if (!found) return null;
      Object mv = values.get("mv");
      int[] mvArr = mv instanceof int[] arr ? arr : mv instanceof List<?> l ? l.stream().mapToInt(x -> (Integer) x).toArray() : null;
      List<String> d1 = (List<String>) values.get("d1");
      List<String> d2 = (List<String>) values.get("d2");
      return JvmMetadataUtil.Metadata(
          (Integer) values.get("k"),
          mvArr,
          d1 == null ? null : d1.toArray(new String[0]),
          d2 == null ? null : d2.toArray(new String[0]),
          (String) values.get("xs"),
          (String) values.get("pn"),
          (Integer) values.get("xi"));
    }
  }

  static boolean pub(Visibility v) {
    return v == Visibility.PUBLIC || v == Visibility.PROTECTED;
  }

  static Map<String, String> owner = new HashMap<>();
  static String mod;
  static Set<String> seen = new HashSet<>();

  static void add(String classifier, String where) {
    String o = owner.get(classifier.replace('.', '$'));
    if (o == null || o.equals(mod)) return;
    if (seen.add(o + "\t" + where)) System.out.println(mod + "\t" + o + "\t" + where);
  }

  static void type(KmType t, String w) {
    if (t == null) return;
    KmClassifier c = t.getClassifier();
    if (c instanceof KmClassifier.Class k) add(k.getName(), w);
    if (c instanceof KmClassifier.TypeAlias k) add(k.getName(), w);
    for (KmTypeProjection p : t.getArguments()) type(p.getType(), w);
    type(t.getAbbreviatedType(), w);
    type(t.getOuterType(), w);
    if (t.getFlexibleTypeUpperBound() != null) type(t.getFlexibleTypeUpperBound().getType(), w);
  }

  static void typeParams(List<KmTypeParameter> tps, String w) {
    for (KmTypeParameter tp : tps) for (KmType b : tp.getUpperBounds()) type(b, w);
  }

  static void params(List<KmValueParameter> ps, String w) {
    for (KmValueParameter p : ps) {
      type(p.getType(), w);
      type(p.getVarargElementType(), w);
    }
  }

  @SuppressWarnings("deprecation")
  static void container(KmDeclarationContainer c, String prefix) {
    for (KmFunction f : c.getFunctions()) {
      if (!pub(Attributes.getVisibility(f))) continue;
      String w = prefix + "." + f.getName();
      type(f.getReceiverParameterType(), w);
      type(f.getReturnType(), w);
      params(f.getValueParameters(), w);
      typeParams(f.getTypeParameters(), w);
      for (KmType t : f.getContextReceiverTypes()) type(t, w);
    }
    for (KmProperty p : c.getProperties()) {
      if (!pub(Attributes.getVisibility(p))) continue;
      String w = prefix + "." + p.getName();
      type(p.getReceiverParameterType(), w);
      type(p.getReturnType(), w);
      typeParams(p.getTypeParameters(), w);
    }
    for (KmTypeAlias ta : c.getTypeAliases()) {
      if (!pub(Attributes.getVisibility(ta))) continue;
      String w = prefix + "." + ta.getName();
      type(ta.getUnderlyingType(), w);
      type(ta.getExpandedType(), w);
    }
  }

  public static void main(String[] args) throws Exception {
    File root = new File(args[0]);
    Map<String, List<Map.Entry<String, KotlinClassMetadata>>> parsed = new LinkedHashMap<>();
    for (int i = 1; i < args.length; i++) {
      String m = args[i].substring(0, args[i].indexOf('='));
      String dir = args[i].substring(args[i].indexOf('=') + 1);
      for (String cd : CLASS_DIRS) {
        File base = new File(root, dir + "/" + cd);
        if (!base.exists()) continue;
        try (var stream = Files.walk(base.toPath())) {
          for (var p : (Iterable<java.nio.file.Path>) stream.filter(x -> x.toString().endsWith(".class"))::iterator) {
            Collector col = new Collector();
            new ClassReader(Files.readAllBytes(p)).accept(col, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            owner.putIfAbsent(col.name, m);
            kotlin.Metadata md = col.metadata();
            if (md == null) continue;
            try {
              parsed.computeIfAbsent(m, k -> new ArrayList<>()).add(Map.entry(col.name, KotlinClassMetadata.readLenient(md)));
            } catch (Exception e) {
              System.err.println("WARN " + col.name + ": " + e);
            }
          }
        }
      }
    }
    for (var e : parsed.entrySet()) {
      mod = e.getKey();
      Map<String, KmClass> classes = new HashMap<>();
      for (var en : e.getValue())
        if (en.getValue() instanceof KotlinClassMetadata.Class c) classes.put(en.getKey(), c.getKmClass());
      for (var en : e.getValue()) {
        String n = en.getKey();
        if (n.contains("$Metro") || n.startsWith("metro/") || n.contains("$$serializer")) continue;
        KotlinClassMetadata km = en.getValue();
        if (km instanceof KotlinClassMetadata.Class c) {
          if (!classPublic(n, classes)) continue;
          KmClass k = c.getKmClass();
          for (KmType s : k.getSupertypes()) type(s, n + " : supertype");
          typeParams(k.getTypeParameters(), n + "<T>");
          for (KmConstructor ctor : k.getConstructors())
            if (pub(Attributes.getVisibility(ctor))) params(ctor.getValueParameters(), n + ".<init>");
          type(k.getInlineClassUnderlyingType(), n + " (value)");
          container(k, n);
        } else if (km instanceof KotlinClassMetadata.FileFacade f) {
          container(f.getKmPackage(), n);
        } else if (km instanceof KotlinClassMetadata.MultiFileClassPart f) {
          container(f.getKmPackage(), n);
        }
      }
    }
  }

  static boolean classPublic(String n, Map<String, KmClass> classes) {
    KmClass k = classes.get(n);
    if (k == null || !pub(Attributes.getVisibility(k))) return false;
    int idx = n.lastIndexOf('$');
    if (idx < 0) return true;
    String outer = n.substring(0, idx);
    return !classes.containsKey(outer) || classPublic(outer, classes);
  }
}
