package reverseRead;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class reverseRead {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		InputStream fis = new FileInputStream
				("C:\\Users\\kimyo\\eclipse-workspace\\reverseRead\\src\\reverseRead\\input.txt");
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		
		FileOutputStream fos = new FileOutputStream("C:\\Users\\kimyo\\eclipse-workspace\\reverseRead\\src\\reverseRead\\output.txt");
		OutputStreamWriter osw=new OutputStreamWriter(fos);
		BufferedWriter bw=new BufferedWriter(osw);
		
		String[] str = new String[100];
		int index=0;
		String data;
		while((data=br.readLine())!=null) {
			str[index]=data;
			index++;
		}
		
		
		
		
		for(int i=index-1;i>=0;i--) {
			bw.write(str[i]);
			bw.newLine();
		}
		
		bw.flush();
		fis.close();br.close();isr.close();
		fos.close();osw.close();bw.close();
	}

}
