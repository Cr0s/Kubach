package kubach.ac;

public class CCLoader extends ClassLoader {

    private byte[] classdata = null;

    public CCLoader() {
        super(CCLoader.class.getClassLoader());
    }

    public void setClassContent(byte[] data) {
        classdata = new byte[data.length];
        System.arraycopy(data, 0, classdata, 0, data.length);
    }

    @Override
    public Class findClass(String n) throws ClassNotFoundException {
        if (n.startsWith("java")) {
            return super.loadClass(n, true);
        }
        
        Class result = null;
        
        try {
            result = defineClass(n, this.classdata, 0, this.classdata.length, null);
            resolveClass(result);
            
            return result;
        } catch (SecurityException se) {
            System.out.println(se.toString());
            result = super.loadClass(n, true);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return result;
    }
}
