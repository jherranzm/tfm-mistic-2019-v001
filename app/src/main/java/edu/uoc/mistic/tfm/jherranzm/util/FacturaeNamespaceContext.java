package edu.uoc.mistic.tfm.jherranzm.util;

import java.util.Collections;

import javax.xml.namespace.NamespaceContext;

public class FacturaeNamespaceContext implements  NamespaceContext{

    @SuppressWarnings("rawtypes")
    @Override
    public java.util.Iterator getPrefixes(final String namespaceURI) {
        return Collections.singleton("fe").iterator();
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        return "fe";
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return "http://www.facturae.es/Facturae/2014/v3.2.1/Facturae";
    }
}
