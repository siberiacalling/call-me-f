package ru.bmstu.rk6.cell.stub;


/**
* Cell/bytesHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Cell.idl
* Wednesday, December 14, 2011 10:06:30 PM MSK
*/

public final class bytesHolder implements org.omg.CORBA.portable.Streamable
{
  public byte value[] = null;

  public bytesHolder ()
  {
  }

  public bytesHolder (byte[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = ru.bmstu.rk6.cell.stub.bytesHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    ru.bmstu.rk6.cell.stub.bytesHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return ru.bmstu.rk6.cell.stub.bytesHelper.type ();
  }

}
