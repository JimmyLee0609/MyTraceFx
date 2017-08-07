package de.method;

public class Modifier {
static String [] modifier=new String[] {"package","public","private","protected","static","final"
					,"super","volatile","transient","native","interface","abstract","stricifp"
					,"synthetic","annotation","enum"};
	public static String modifier(int modifiers) {
		int b=modifiers;
		StringBuilder builder = new StringBuilder();
		if(0==modifiers) {
			return "";
		}
		for(int i=1;i<16;i++) {

			if(((~b&0x0001)<<32)==0) {
				builder.append(modifier[i]+" ");
			}

			b=b>>1;
		}
		return builder.toString();
	}

}
