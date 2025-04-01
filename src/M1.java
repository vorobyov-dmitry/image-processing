import java.lang.reflect.Field;

public class M1 {

	public static void main(String[] args) {

	}

	private static void test01(float[] f, int x, float y) {
		f[0]= 40f;
		x=x*2;
		y=y*4;
		
	}

	public static void testIntegerCache() {
		Class<?> clazz;
		try {
			clazz = Class.forName("java.lang.Integer$IntegerCache");
			Field field = clazz.getDeclaredField("cache");
		    field.setAccessible(true);
		    Integer[] cache = (Integer[]) field.get(clazz);
		    System.out.println(cache[5+127]);
		    cache[5+127]=6;
	} catch (ClassNotFoundException | NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			    
		
		Integer i1 = 128;
		Integer i2 = 128;
		System.out.println(i1 == i2);
		Integer i3 = 4;
		Integer i4 = 2+2;
		String name;
		System.out.println(i3);
		System.out.println(i3 == i4);
		System.out.println(""+i3.hashCode()+" "+ i4.hashCode());
		System.out.println(i3.hashCode() == i4.hashCode());
		
		System.out.println(boolean.class.getName());
		System.out.println(Boolean.class.getName());
		long l1 = System.currentTimeMillis();
		for(int i = 100000; i > 0; i--) {}
		long l2 = System.currentTimeMillis();
		for(int i = 1; i < 100001; i++) {}
		long l3 = System.currentTimeMillis();
		System.out.println((l2-l1));
		System.out.println((l3-l2));
	}

}
