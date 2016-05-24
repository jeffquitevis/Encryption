import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.*;
import java.util.*;

/**
 * Created by stephen on 4/28/2016.
 */
public class App {

    public static final String PUBLIC_KEY_FILE = "c:/keys/public.keys";
    public static final String PRIVATE_KEY_FILE = "c:/keys/private.keys";
    public static final String ALGORITHM = "RSA";
    public static final String FILE_RECORD = "c:/temp/encryption.txt";
    public static  Map<Integer,Integer> map = new HashMap<Integer,Integer>();



    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException {

//        generateKeys();

        encrypt(new Person(1,"jeff stephen quitevis"));
        encrypt(new Person(2,"marcus alexander quitevis"));
        encrypt(new Person(3,"susan quitevis"));

        Person temp = decrypt(3);

        System.out.print(temp.getName());

    }


    public static byte[] encrypt(Person person) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, ClassNotFoundException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {


        Cipher cipher = Cipher.getInstance(ALGORITHM);
        PublicKey publicKey = null;
        File file = new File(FILE_RECORD);

        //GET THE PUBLIC KEY
            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE))){

                publicKey = (PublicKey) ois.readObject();
            }

        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();



        //WRITE TO BUFFER -> ENCRYPT -> WRITE TO FILE
            try(DataOutputStream dosMemory = new DataOutputStream(baos)){

                dosMemory.writeInt(person.getId());
                dosMemory.writeUTF(person.getName());

                byte[] tempCipherSize =  cipher.doFinal(baos.toByteArray());


                try(DataOutputStream dosFile =new DataOutputStream(new FileOutputStream(file,true))){


                        map.put(person.getId(),(int) file.length());

                        dosFile.write(tempCipherSize);

                }

            }


        return null;
    }

    public static Person decrypt(int id) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        PrivateKey privateKey = null;
        Person output = null;
        byte[] temp = null;

            try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE))){

                privateKey = (PrivateKey) ois.readObject();
            }

        cipher.init(Cipher.DECRYPT_MODE,privateKey);

        byte[] buffer = new byte[128];

            try(DataInputStream disFile = new DataInputStream(new FileInputStream(FILE_RECORD))){
                disFile.skipBytes(map.get(id));
                disFile.read(buffer);

                temp = cipher.doFinal(buffer);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try(DataOutputStream dosMemory = new DataOutputStream(baos)){

                    dosMemory.write(temp);
                }

                try(DataInputStream disMemory = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()))){

                    output = new Person(disMemory.readInt(),disMemory.readUTF());
                }

            }

        return output;
    }


    public static void generateKeys() throws NoSuchAlgorithmException, IOException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
        keyGen.initialize(1024);

        KeyPair keyPair = keyGen.generateKeyPair();
        File publicKeyFile = new File(PUBLIC_KEY_FILE);
        File privateKeyFile = new File(PRIVATE_KEY_FILE);

        if (privateKeyFile.getParentFile() != null){

            privateKeyFile.getParentFile().mkdir();
        }
        privateKeyFile.createNewFile();

        if (publicKeyFile.getParentFile() != null){

            publicKeyFile.getParentFile().mkdir();
        }

        publicKeyFile.createNewFile();


        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(publicKeyFile))){

            PublicKey publicKey = keyPair.getPublic();
            oos.writeObject(publicKey);

        }

        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(privateKeyFile))){

            PrivateKey privateKey = keyPair.getPrivate();
            oos.writeObject(privateKey);

        }

    }

}
