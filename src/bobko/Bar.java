package bobko;

import java.io.*;

public class Bar {
    public static void main(String[] args) throws ClassNotFoundException {
        CustomClassLoader custom = new CustomClassLoader(Bar.class.getClassLoader());
//        Class<?> base = Class.forName("bobko.Base", true, custom);
        Class<?> aClass = Class.forName("bobko.Foo", true, custom);
        System.out.println(aClass + " " + aClass.getClassLoader());
        Class<?> superclass = aClass.getSuperclass();
        System.out.println(superclass + " " + superclass.getClassLoader());
    }
}


class CustomClassLoader extends ClassLoader {
    public CustomClassLoader(ClassLoader parent) {
        this(parent, null);
    }

    public CustomClassLoader(ClassLoader parent, Class<?> defined) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("--- Class Loading Started for " + name);
        if ("bobko.Base".equals(name)) {
            return super.loadClass(name);
        }
        if (name.startsWith("bobko")) {
            return getClass(name);
        }
        return super.loadClass(name);
    }

    /**
     * Loading of class from .class file
     * happens here You Can modify logic of
     * this method to load Class
     * from Network or any other source
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    private Class<?> getClass(String name) throws ClassNotFoundException {

        String file = name.replace('.', File.separatorChar) + ".class";
        byte[] byteArr = null;
        try {
            // This loads the byte code data from the file
            byteArr = loadClassData(file);
            Class<?> c = defineClass(name, byteArr, 0, byteArr.length);
            resolveClass(c);
            return c;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a given file and converts
     * it into a Byte Array
     * @param name
     * @return
     * @throws IOException
     */
    private byte[] loadClassData(String name) throws IOException {

        InputStream stream = getClass().getClassLoader().getResourceAsStream(
                name);
        int size = stream.available();
        byte buff[] = new byte[size];
        DataInputStream in = new DataInputStream(stream);
        // Reading the binary data
        in.readFully(buff);
        in.close();
        return buff;
    }
}
