import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public interface SQLReflector
{
    //@Override this method for custom configuration
    //Default return 0 for mute annoying @Override
    default String[] getEscapeField(){ return new String[0]; }

    default String[] getKeys()
    {
        List<Field> fields = Arrays.asList(this.getClass().getFields());
        List<String> escapeFields = Arrays.asList(getEscapeField());

        for(String fieldName : escapeFields)
            for(Field field : fields)
                if(field.getName().equals(fieldName))
                    fields.remove(field);

        String[] keys = new String[fields.size()];
        for(int i = 0; i < fields.size(); ++i)
            keys[i] = fields.get(i).getName();

        return keys;
    }

    default Object[] getValues()
    {
        String[] keys = getKeys();
        if(keys != null && keys.length > 0)
        {
            Object[] values = new Object[keys.length];

            for(int i = 0; i < keys.length; ++i)
            {
                for(Field field : this.getClass().getFields())
                {
                    if(keys[i].equals(field.getName()))
                    {
                        try
                        {
                            values[i] = field.get(this);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return values;
        }
        else
        {
            if(keys == null)
                throw new IllegalStateException("Key (String[]) cannot be null !");
            else
                throw new IllegalStateException("Key (String[]) are you confirm it is ZERO length ?");
        }
    }

    default String getInsertStatement(String tableName)
    {
        return String.format("INSERT INTO %s(%s) VALUES (%s);", tableName, StringUtils.join(getKeys(), ", "), StringUtils.join(getValues(), ", "));
    }
}
