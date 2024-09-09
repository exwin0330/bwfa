package sootAnalyzer.code;

public class Example {

	public static void main(String[] args) {
		int a = 1;
		
		if(a == 2){
			System.out.println(a);
		}
		else if(a > 2){
			System.out.println(--a);
		}
		else{
			System.out.println(++a);
		}
	}
}
