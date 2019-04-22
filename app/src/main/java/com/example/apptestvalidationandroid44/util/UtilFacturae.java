package com.example.apptestvalidationandroid44.util;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.ByteArrayInputStream;

import es.facturae.facturae.v3.facturae.Facturae;

public class UtilFacturae {

    public static Facturae getFacturaeFromFactura(String factura){
        try{
            IBindingFactory bindingFactory = BindingDirectory.getFactory(Facturae.class);
            IUnmarshallingContext unmarshallingContext = bindingFactory.createUnmarshallingContext();

            return (Facturae)unmarshallingContext.unmarshalDocument(new ByteArrayInputStream(factura.getBytes()), null);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
