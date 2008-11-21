package org.jboss.resteasy.test.finegrain.methodparams;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.spi.StringConverter;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ext.Provider;

public class SuperStringConverterTest extends BaseResourceTest
{
   public static class Person
   {
      private final String name;

      public Person(String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }
   }

   @Provider
   public static class PersonConverter extends SuperPersonConverter
   {
   }

   public abstract static class SuperPersonConverter implements StringConverter<Person>
   {
      public Person fromString(String value)
      {
         return new Person(value);
      }

      public String toString(Person value)
      {
         return value.getName();
      }
   }

   public static class Company
   {
      private final String name;

      public Company(String name)
      {
         this.name = name;
      }

      public String getName()
      {
         return name;
      }

      @Override
      public String toString()
      {
         return getName();
      }
   }

   // remove implements to trigger RESTEASY-160
   @Provider
   public static class CompanyConverter extends ObjectConverter<Company> implements StringConverter<Company>
   {
      public Company fromString(String value)
      {
         return new Company(value);
      }

   }

   public abstract static class ObjectConverter<T> implements StringConverter<T>
   {
      public String toString(T value)
      {
         return value.toString();
      }
   }

   @Path("/")
   public static class MyResource
   {
      @Path("person/{person}")
      @PUT
      public void put(@PathParam("person") Person p)
      {
         Assert.assertEquals(p.getName(), "name");
      }

      @Path("company/{company}")
      @PUT
      public void putCompany(@PathParam("company") Company c)
      {
         Assert.assertEquals(c.getName(), "name");
      }
   }

   @Before
   public void setUp() throws Exception
   {
      dispatcher.getProviderFactory().addStringConverter(PersonConverter.class);
      dispatcher.getProviderFactory().addStringConverter(CompanyConverter.class);
      dispatcher.getRegistry().addPerRequestResource(MyResource.class);
   }

   @Path("/")
   public static interface MyClient
   {
      @Path("person/{person}")
      @PUT
      void put(@PathParam("person") Person p);

      @Path("company/{company}")
      @PUT
      public void putCompany(@PathParam("company") Company c);
   }

   @Test
   public void testPerson() throws Exception
   {
      MyClient client = ProxyFactory.create(MyClient.class, "http://localhost:8081");
      Person person = new Person("name");
      client.put(person);
   }

   @Test
   public void testCompany() throws Exception
   {
      MyClient client = ProxyFactory.create(MyClient.class, "http://localhost:8081");
      Company company = new Company("name");
      client.putCompany(company);
   }
}
