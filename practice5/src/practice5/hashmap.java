package practice5;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;

import java.util.Scanner;

public class hashmap {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<>();
		while(true) {
			
			Scanner sc = new Scanner(System.in);
			String str = sc.nextLine();
			
			String[] array = str.split(" ");
			if(array.length==1) {
				ArrayList<String> keyList=new ArrayList<>(map.keySet());
				keyList.sort(null);
				Iterator<String> keys=keyList.iterator();
				while(keys.hasNext()) {
					String key = keys.next();
					System.out.println(key+ " "+map.get(key));
				}
				
			}
			else if (array.length==2) {
				
				if(array[0].equals("delete")) {
					if(map.containsKey(array[1])) {
						map.remove(array[1]);
					}
					else {
						System.out.println("error");
					}
				}
				else if (array[0].equals("find")) {
					
					if(map.containsKey(array[1])) {
						
						System.out.println(map.get(array[1]));
					}
					else {
						
						System.out.println("error");
					}
				}
			}
			
			else {
				
				if(map.containsKey(array[1])) {
					System.out.println("error");
				}
				else {
					map.put(array[1], array[2]);
					
				}
			}
			
		}
		
		
	}

}
