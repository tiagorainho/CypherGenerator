import static java.lang.System.*;
import java.util.*;

public class Functions
{  
    protected String name;
    protected String returnType;
    protected ArrayList<String> ArgTypes;

    public Functions(String n,String returnT)
    {
        this.name = n;
        this.returnType = returnT; 
        ArgTypes = new ArrayList<>();
    }
    public void addType(String type)
    {
        ArgTypes.add(type);
    }
    public String getName()
    {
        return name;
    }
    public ArrayList<String> getArray()
    {
        return ArgTypes;
    }
    public String getType()
    {
        return this.returnType;
    }
    public boolean equal(Functions f)
    {
        if(name.equals(f.getName()) && ArgTypes.equals(f.getArray()) && returnType.equals(f.getType()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public String frase()
    {
        return name + " " + returnType + ArgTypes;
    }

   
}