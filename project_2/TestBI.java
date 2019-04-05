import java.math.BigInteger;

public class TestBI {
	public static void main(String[] args) {
		String s = "f";
		BigInteger num = new BigInteger(s, 16);
		System.out.println(num.toString(16));
	}
	

}