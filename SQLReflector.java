package com.horuslinkz.zcore.interfaces;

import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public interface SQLGenerator
{
    default void debug()
    {
        System.out.println("My Fields Size: " + this.getClass().getDeclaredFields().length);

        for(Field field : this.getClass().getDeclaredFields())
        {
            try
            {
                field.setAccessible(true);
                System.out.println(field.getType().getName() + " - " + field.getName() + " : " + field.get(this));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    default boolean shouldSkip(String field){ return false; }

    default String[] getKeys()
    {
        Field[] fields = this.getClass().getDeclaredFields();
        List<String> keys = new ArrayList<>();
        
        for(Field field : fields)
            if(!shouldSkip(field.getName()))
                keys.add(field.getName());

        return keys.toArray(new String[keys.size()]);
    }

    default Object[] getValues()
    {
        String[] keys = getKeys();
        if(keys != null && keys.length > 0)
        {
            Object[] values = new Object[keys.length];

            for(int i = 0; i < keys.length; ++i)
            {
                for(Field field : this.getClass().getDeclaredFields())
                {
                    if(keys[i].equals(field.getName()))
                    {
                        try
                        {
                            field.setAccessible(true);
                            Object value = field.get(this);

                            if(value != null)
                            {
                                if(field.getType().getName().equals("java.lang.String") && !value.equals("NOW()"))
                                {
                                    values[i] = "'" + String.valueOf(value) + "'";
                                }
                                else
                                {
                                    values[i] = field.get(this);
                                }
                            }
                            else
                            {
                                values[i] = "NULL";
                            }
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

//Exec:
//Customer c = new Customer("First Name", "Last Name", "Email", "Telephone");
//System.out.println(c.getInsertStatement("Table"));

//Output:
//INSERT INTO Table(customer_id, customer_group_id, store_id, language_id, firstname, lastname, email, telephone, fax, password, salt, cart, wishlist, newsletter, address_id, custom_field, ip, status, approved, safe, token, code, date_added) VALUES (0, 1, 1, 1, 'First Name', 'Last Name', 'Email', 'Telephone', '', NULL, NULL, '', '', true, 0, NULL, NULL, true, true, false, NULL, NULL, NOW());

//Input:
// public class Customer extends BaseModel implements SQLGenerator
// {
//     private int customer_id;
//     private int customer_group_id;
//     private int store_id;
//     private int language_id;
//     private String firstname;
//     private String lastname;
//     private String email;
//     private String telephone;
//     private String fax;
//     private String password;
//     private String salt;
//     private String cart;
//     private String wishlist;
//     private boolean newsletter;
//     private int address_id;
//     private String custom_field;
//     private String ip;
//     private boolean status;
//     private boolean approved;
//     private boolean safe;
//     private String token;
//     private String code;
//     private String date_added = "NOW()";
// }
