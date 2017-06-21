package com.prinego.ontology.object2ontology.api;

import java.lang.reflect.Field;
import java.util.Collection;

import javax.inject.Inject;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.prinego.domain.entity.ontology.medium.Medium;
import com.prinego.ontology.handler.MyOntologyService;
import com.prinego.ontology.object2ontology.domain.MyOntologyPropertyMeta;
import com.prinego.ontology.object2ontology.domain.MyOntologyPropertyType;
import com.prinego.ontology.object2ontology.domain.reflection.MyField;
import com.prinego.ontology.object2ontology.domain.reflection.MyFieldType;
import com.prinego.ontology.object2ontology.util.IndividualNameCreator;
import com.prinego.ontology.object2ontology.util.KeyGenerator;
import com.prinego.ontology.object2ontology.util.MyReflectionUtil;
import com.prinego.ontology.object2ontology.util.Object2OntologyUtil;

/**
 * Created by mester on 16/08/14.
 */
@Component
public class Object2OntologyServiceImpl implements Object2OntologyService {

    @Inject
    private IndividualNameCreator indNameCreator;

    @Inject
    private MyOntologyService myOntologyService;

    @Override
    public String upsertMyObject(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj) {

        Preconditions.checkNotNull(obj);

        String key = KeyGenerator.generateKey();
        OWLNamedIndividual ind_obj = myOntologyService.applyClassAssertionFromObject(ontManager, dataFactory, myOntology, pm, obj, indNameCreator.createIndividualName(obj, key));
        upsertMyObjectFields(ontManager, dataFactory, myOntology, pm, obj, ind_obj, key);

        try {
            ontManager.saveOntology(myOntology);
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }

        return key;
    }

    @Override
    public void upsertMyObjectFields(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj,
            OWLNamedIndividual ind_obj,
            String key) {

        // Check the preconditions
        Preconditions.checkNotNull(ontManager);
        Preconditions.checkNotNull(dataFactory);
        Preconditions.checkNotNull(myOntology);
        Preconditions.checkNotNull(pm);
        Preconditions.checkNotNull(obj);
        Preconditions.checkNotNull(ind_obj);
        Preconditions.checkNotNull(key);

        Class clazz = obj.getClass();

        Field[] fields = clazz.getDeclaredFields();
        if ( clazz.getSuperclass().equals(Medium.class) ) {		//TODO adapt to the new ontology
            fields = Medium.class.getDeclaredFields();
        }
        for ( Field field : fields ) {

          if ( "exampleName".equals(field.getName()) ) {
                continue; // PostRequest.exampleName is only for test purposes, not related with ontology.
            }
            
            if ( "negotiationMethod".equals(field.getName()) ) {
                continue; // PostRequest.negotiationMethod is only for method purposes, not related with ontology.
            }
            
            if("utilityThreshold".equals(field.getName())){
            	continue; //We dont need this on ontology
            }

            MyField myField = MyReflectionUtil.convertFieldToMyField(field);
            MyOntologyPropertyMeta propertyMeta = Object2OntologyUtil.getMyOntologyPropertyMeta(myField);

            if ( MyOntologyPropertyType.DATA_PROPERTY.equals(propertyMeta.getPropertyType()) ) {

                String dataPropertyName = propertyMeta.getPropertyName();

                if ( MyFieldType.PLURAL_ORDINARY.equals(myField.getType()) ) {

                    System.out.println("NOT IMPLEMENTED YET 1, THERE WAS NO NEED FOR NOW!!!");
                    // TODO if I need a data property that is not functional.
                } else {
                    Object data = MyReflectionUtil.convertFieldToObject(obj, field);
                    if (data != null) {
                        myOntologyService.applyDataPropertyAssertion(ontManager, dataFactory, myOntology, pm, ind_obj, dataPropertyName, data);
                    }
                }

            } else if ( MyOntologyPropertyType.OBJECT_PROPERTY.equals(propertyMeta.getPropertyType()) ) {

                String objectPropertyName = propertyMeta.getPropertyName();

                if ( MyFieldType.PLURAL_MY_ONTO_OBJECT.equals(myField.getType()) ) {

                    // alternativeMediums, audienceMembers

                    Collection fieldColl = MyReflectionUtil.convertFieldToCollection(obj, field);
                    int count = 1;
                    if ( fieldColl != null ) {
                        for (Object fieldObj : fieldColl) {
                            String fieldIndName = indNameCreator.createIndividualName(fieldObj, key);
                            if ( "hasAlternativeMedium".equals(objectPropertyName) ) {
                                fieldIndName = indNameCreator.createIndividualName(fieldObj, key + "_" + count);
                            }

                            OWLObjectProperty obj_prop = dataFactory.getOWLObjectProperty(propertyMeta.getPropertyName(), pm);
                            OWLNamedIndividual ind_field_obj = myOntologyService.applyClassAssertionFromObject(ontManager, dataFactory, myOntology, pm, fieldObj, fieldIndName);
                            OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(obj_prop, ind_obj, ind_field_obj);
                            ontManager.applyChange(new AddAxiom(myOntology, objectPropertyAssertionAxiom));


                            //upsertMyObjectFields(ontManager, dataFactory, myOntology, pm, fieldObj, fieldKey);
                            upsertMyObjectFields(ontManager, dataFactory, myOntology, pm, fieldObj, ind_field_obj, key);

                            count++;
                        }
                    }

                } else if ( MyFieldType.SINGLE_MY_ONTO_OBJECT.equals(myField.getType()) ) {

                    Object fieldObj = MyReflectionUtil.convertFieldToObject(obj, field);
                    if ( fieldObj != null ) {
                        OWLNamedIndividual ind_field_obj = dataFactory.getOWLNamedIndividual(indNameCreator.createIndividualName(fieldObj, key), pm);
                        myOntologyService.applyObjectPropertyAssertion(ontManager, dataFactory, myOntology, pm, ind_obj, objectPropertyName, ind_field_obj);
                        upsertMyObjectFields(ontManager, dataFactory, myOntology, pm, fieldObj, ind_field_obj, key);
                    }
                }
            }
        }
    }

}
