package uk.co.airelogic;

import ca.uhn.fhir.context.FhirContext;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.BaseResource;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Main {



    public static void main(String[] args) {
	// write your code here



        String fileName = "C:/Temp/QRISK-ME.json";
        String contents = "";

        if (isNotBlank(fileName)) {
            String encoding = "UTF-8";

            try {
                contents = IOUtils.toString(new InputStreamReader(new FileInputStream(fileName), encoding));
                load(contents);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

        }
    }

    public static void load(String contents) throws Exception {
        FhirContext ctx = FhirContext.forDstu3();

        Map<String,String> map = new HashMap<>();

        ca.uhn.fhir.rest.api.EncodingEnum enc = ca.uhn.fhir.rest.api.EncodingEnum.detectEncodingNoDefault(defaultString(contents));
        if (enc == null) {
            throw new ParseException("Could not detect encoding (json/xml) of contents");
        }
        IBaseResource
                resource = ctx.newJsonParser().parseResource(contents);

        if (resource instanceof Bundle) {
            Bundle bundle = (Bundle) resource;
            bundle.setType(Bundle.BundleType.COLLECTION);

            for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                String id = entry.getResource().getIdElement().getIdPart();
                entry.getResource().setId(java.util.UUID.randomUUID().toString());
                entry.setFullUrl("urn:uuid:"+entry.getResource().getId());
                map.put(entry.getResource().getResourceType()+"/"+id,"urn:uuid:"+entry.getResource().getId());
            }



            String output = ctx.newJsonParser().setPrettyPrint(true).encodeResourceToString(bundle);

            for (Map.Entry<String, String> value : map.entrySet()) {
                System.out.println(value.getKey() + " " +value.getValue());
                output = output.replace("\""+value.getKey()+"\"","\""+value.getValue()+"\"");
            }

           System.out.println(output);
            FileOutputStream fop = new FileOutputStream("C:/Temp/QRISK-ME-COL.json");
            byte[] contentInBytes = output.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        }
    }
}
