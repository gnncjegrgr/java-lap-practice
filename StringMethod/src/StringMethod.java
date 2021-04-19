
public class StringMethod {
	static String addString(String s1, int index, String s2) {
		String s3="";
		for(int i=0;i<s1.length();i++) {
			if(i!=index) {
				s3+=s1.charAt(i);
			}
			else {
				
				s3+=s1.charAt(i);
				s3+=s2;
			}
		}
		return s3;
	}
	
	static String reverse(String s) {
		String s3="";
		for(int i=s.length()-1;i>=0;i--) {
			s3+=s.charAt(i);
		}
		return s3;
	}
	
	static String removeString(String s1, String s2)  {
		String s3=s1.replace(s2, "");
		return s3;
	}
	
	public static void main(String[] args) {
		String s1="0123456";
		int a=3;
		String s2="-";
		String s3="abc";
		String s4="01001000";
		String s5="00";
		System.out.println(addString(s1,a,s2));
		System.out.println(reverse(s3));
		System.out.println(removeString(s4,s5));
				
	}
}
