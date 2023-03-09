package minigame.core;

import java.io.IOException;
import java.lang.reflect.Method;

public class Util {
    private final static char[] charMap={'a','b','c','d','e','f','g','h','i','j','k','m','n','o','p',
            'q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L',
            'M','N','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9'};
    /**
     *
     * @return 压缩后的字符串
     */
    public static String zipAddress(String ip,int port){
        String[] childs=ip.split("\\.");
        int size=charMap.length;
        int node;
        char[] rawOutPut=new char[9];
        //序列化ip
        for (int i=0;i<4;i++){
            if (i<2&&childs[0].equals("192")&&childs[1].equals("168")){
                rawOutPut[2]= ((char) (size - 1));
                i=1;
                continue;
            }
            node=Integer.parseInt(childs[i]);
            //进位放标志位
            if (node>=size){
                //放在标志位高位
                if (i%2==0){
                    rawOutPut[i<2?0:3]+=node/size*10;
                }else { //放低位
                    rawOutPut[i<2?0:3]+=node/size;
                }
            }
            rawOutPut[i<2?i+1:i+2]= (char) (node%size);
        }
        //序列化端口
        rawOutPut[6]= (char) (port/size/size);
        rawOutPut[7]= (char) ((port/size)%size);
        rawOutPut[8]= (char) (port%size);
        for (int i=0;i<rawOutPut.length;i++){
            rawOutPut[i]=charMap[rawOutPut[i]];
        }
        if (childs[0].equals("192")&&childs[1].equals("168")){
            return new String(rawOutPut,2,7);
        }else {
            return new String(rawOutPut);
        }
    }

    /**
     *
     * @param zipped
     * @return 第一个是String ip，第二个是Integer port
     */
    public static Object[] unZipAddress(String zipped) throws IllegalArgumentException{
        try {
            char[] chars=zipped.toCharArray();
            char a;
            int size= charMap.length;
            for (int i=0;i<chars.length;i++){
                a=chars[i];
                for (int j=0;j<size;j++){
                    if (charMap[j]==a){
                        chars[i]=(char) j;
                        break;
                    }
                }
            }

            int p1,p2,p3,p4;
            if (chars[0]==size-1){
                p1=192;
                p2=168;
                p3=chars[1]/10*size+chars[2];
                p4=chars[1]%10*size+chars[3];
            }else {
                p1=chars[0]/10*size+chars[1];
                p2=chars[0]%10*size+chars[2];
                p3=chars[3]/10*size+chars[4];
                p4=chars[3]%10*size+chars[5];
            }
            Object[] out=new Object[2];
            out[0]=p1+"."+p2+"."+p3+"."+p4;
            out[1]=chars[chars.length-3]*size*size+chars[chars.length-2]*size+chars[chars.length-1];
            return out;
        }catch (Exception e){
            throw new IllegalArgumentException();
        }
    }

    /**
     * https://blog.csdn.net/xingbaozhen1210/article/details/81078101
     * @param url
     */
    public static void browse(String url) throws Exception {
        String os=System.getProperty("os.name","");
        if (os.startsWith("Windows")){
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandle "+url);
        }else if (os.startsWith("Mac OS")){
            Class fmgr=Class.forName("com.apple.eio.FileManager");
            Method open=fmgr.getDeclaredMethod("openURL",String.class);
            open.invoke(null,url);
        }else {//Linux不支持
            throw new UnsupportedOperationException();
        }
    }
}
