package za.co.discoverylife.desktop.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class ObjectHelper {
	private HashMap<String, FieldHelper> fldMap;
	
	public ObjectHelper(Class<?> klass){
		this(klass,true);
	}

	public ObjectHelper(Class<?> klass, boolean doParentClass) {
		fldMap = new HashMap<String, FieldHelper>();
		Class<?> k = klass;
		while (k != null) {
			Field[] flds = k.getDeclaredFields();
			for (Field f : flds) {
				// skip static fields and transient fields
				int mod = f.getModifiers();
				if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
					continue;
				}
				FieldHelper fa = new FieldHelper(f);
				fldMap.put(f.getName(), fa);
			}
			if (!doParentClass) {
				break;
			}
			k = k.getSuperclass();
			if (k == null || k.equals(Object.class)) {
				break;
			}
		}
	}
	
	public Object getFieldObject(Object object,String fieldname) throws Exception{
		FieldHelper fh = fldMap.get(fieldname);
		if(fh==null) throw new Exception("Invalid field name '"+fieldname+"'");
		return fh.getField().get(object);
	}
	
	public void setFieldObject(Object object,String fieldname,Object value) throws Exception{
		FieldHelper fh = fldMap.get(fieldname);
		if(fh==null) throw new Exception("Invalid field name '"+fieldname+"'");
		fh.getField().set(object,value);
	}
}
