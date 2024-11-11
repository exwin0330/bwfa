public class Test {
	
	public static void main(String[] args) {
		int i0 = 0;
		int i1 = 1;

		while (i0 < 10) {
			i0 = i0 + i1;
			i1 = i0;
		}
		return;
	}
}
