package za.co.discoverylife.desktop.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import za.co.discoverylife.desktop.host.ServerConnection;

public class AccessHelper {
	private HashMap<String, FieldHelper> fldMap;
	
	public AccessHelper(Class<?> klass){
		this(klass,true);
	}

	public AccessHelper(Class<?> klass, boolean doParentClass) {
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

	public class FieldHelper {
		private Field field;

		public FieldHelper(Field field) {
			this.field = field;
			this.field.setAccessible(true);
		}

		public Field getField() {
			return field;
		}

	}

	
//	public static void main(String[] args) {
//		try {
//			AccessHelper ah = new AccessHelper(ServerConnection.class);
//			ServerConnection sc = new ServerConnection("localhost", 80, "anton", "password");
//			ServerConnection sc2 = new ServerConnection("remotehost", 80, "anton", "password");
//			String url = (String) ah.getFieldObject(sc, "url");
//			System.out.println("url="+url);
//			url = (String) ah.getFieldObject(sc2, "url");
//			System.out.println("url2="+url);
//			System.out.println(sc);
//			ah.setFieldObject(sc,"user","another");
//			System.out.println(sc);
//			
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
