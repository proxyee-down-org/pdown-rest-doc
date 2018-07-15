import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.pdown.rest.form.CreateTaskForm;

public class DocBuild {

  public static void main(String[] args) {
    System.out.println(buildSwaggerDefinition(CreateTaskForm.class));
  }

  public static String buildSwaggerDefinition(Class<?> clazz) {
    return buildSwaggerDefinition(clazz, clazz.getGenericSuperclass(), clazz.getSimpleName(), 0);
  }

  private static String buildSwaggerDefinition(Class<?> clazz, Type type, String fieldName, int level) {
    StringBuilder sb = new StringBuilder();
    StringBuilder tab1 = new StringBuilder();
    for (int i = 0; i < level; i++) {
      tab1.append("    ");
    }
    StringBuilder tab2 = new StringBuilder(tab1 + "    ");
    FieldInfo fieldInfo = getFieldInfo(clazz);
    if (fieldName != null) {
      sb.append(tab1 + fieldName + ":\n");
    }
    sb.append(tab2 + "type: \"" + fieldInfo.getType() + "\"\n");
    if (fieldInfo.getFormat() != null) {
      sb.append(tab2 + "format: \"" + fieldInfo.getFormat() + "\"\n");
    }
    if ("object".equals(fieldInfo.getType()) && fieldInfo.getFormat() == null) {
      sb.append(tab2 + "properties:\n");
      for (Field field : clazz.getDeclaredFields()) {
        if ((field.getModifiers() & (Modifier.FINAL | Modifier.TRANSIENT)) == 0) {
          sb.append(buildSwaggerDefinition(field.getType(), field.getGenericType(), field.getName(), level + 1));
        }
      }
    }
    if ("array".equals(fieldInfo.getType())) {
      if (type instanceof ParameterizedType) {
        sb.append(tab2 + "items:\n");
        Class<?> actualClazz = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
        sb.append(buildSwaggerDefinition(actualClazz, actualClazz.getGenericSuperclass(), null, level + 1));
      }
    }
    return sb.toString();
  }

  private static FieldInfo getFieldInfo(Class<?> clazz) {
    if (clazz == int.class || clazz == Integer.class) {
      return new FieldInfo("integer", "int32");
    } else if (clazz == long.class || clazz == Long.class) {
      return new FieldInfo("integer", "int64");
    } else if (clazz == float.class || clazz == Float.class) {
      return new FieldInfo("number", "float32");
    } else if (clazz == double.class || clazz == Double.class) {
      return new FieldInfo("number", "float64");
    } else if (clazz == boolean.class || clazz == Boolean.class) {
      return new FieldInfo("boolean", null);
    } else if (clazz == String.class) {
      return new FieldInfo("string", null);
    } else if (Collection.class.isAssignableFrom(clazz)) {
      return new FieldInfo("array", null);
    } else if (clazz == Map.class) {
      return new FieldInfo("object", "{key:value}");
    } else {
      return new FieldInfo("object", null);
    }
  }

  private static class FieldInfo {

    private String type;
    private String format;

    public FieldInfo(String type, String format) {
      this.type = type;
      this.format = format;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }
  }

  private static class TestForm {

    private List<String> testList;
  }
}

