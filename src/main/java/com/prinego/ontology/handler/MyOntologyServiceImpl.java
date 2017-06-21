package com.prinego.ontology.handler;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.prinego.domain.entity.ontology.agent.Agent;
import com.prinego.domain.entity.rule.RuleForPrinego;
import com.prinego.util.globals.AppGlobals;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by mester on 24/08/14.
 */
@Component
public class MyOntologyServiceImpl implements MyOntologyService {

    @Override
    public OWLOntology readMyOntology(OWLOntologyManager ontManager, String owlFilePath) {

        File myOntologyFile = new File(owlFilePath);

        OWLOntology myOntology = null;
        try {
            myOntology = ontManager.loadOntologyFromOntologyDocument(myOntologyFile);
            //myOntology = ontManager.loadOntology(IRI.create(owlFilePath)); // to load from url, but we cannot write to a url connection
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        return myOntology;
    }

    @Override
    public OWLNamedIndividual applyClassAssertionFromObject(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            Object obj,
            String indName) {

        // Get the class
        Class clazz = obj.getClass();
        OWLClass owl_class_of_obj = dataFactory.getOWLClass(clazz.getSimpleName(), pm);

        // Get the individual
        OWLNamedIndividual ind_obj = dataFactory.getOWLNamedIndividual(indName, pm);

        // create and apply the class assertion
        OWLClassAssertionAxiom classAssertionAxiom = dataFactory.getOWLClassAssertionAxiom(owl_class_of_obj, ind_obj);
        ontManager.applyChange(new AddAxiom(myOntology, classAssertionAxiom));

        // return the individual
        return ind_obj;
    }

    @Override
    public void applyObjectPropertyAssertion(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_obj,
            String objectPropertyName,
            OWLNamedIndividual ind_field_obj) {

        OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = getObjectPropertyAssertionAxiom(
                dataFactory, pm, ind_obj, objectPropertyName, ind_field_obj);

        // apply the object property assertion
        ontManager.applyChange(new AddAxiom(myOntology, objectPropertyAssertionAxiom));
    }

    @Override
    public OWLObjectPropertyAssertionAxiom getObjectPropertyAssertionAxiom(
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            OWLNamedIndividual ind_object) {

        OWLObjectProperty objectProperty = getObjectProperty(propName, dataFactory, pm);

        // create the object property assertion
        OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, ind_subject, ind_object);

        return objectPropertyAssertionAxiom;
    }

    @Override
    public void applyDataPropertyAssertion(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_obj,
            String dataPropertyName,
            Object data) {


        // create the data property assertion
        OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = getDataPropertyAssertionAxiom(dataFactory, pm, ind_obj, dataPropertyName, data);

        // apply the data property assertion
        if ( dataPropertyAssertionAxiom != null ) {
            ontManager.applyChange(new AddAxiom(myOntology, dataPropertyAssertionAxiom));
        }
    }

    @Override
    public OWLDataPropertyAssertionAxiom getDataPropertyAssertionAxiom(
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            Object data) {

        OWLDataProperty dataProperty = getDataProperty(propName, dataFactory, pm);

        // create the data property assertion
        OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = null;
        if ( data instanceof String ) {
            String data_as_string = (String) data;
            dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, ind_subject, data_as_string);
        } else if ( data instanceof Date) {
            Date data_as_date = (Date) data;
            SimpleDateFormat formatter = new SimpleDateFormat(AppGlobals.OWL_DATE_FORMAT);
            String data_as_formatteddate = formatter.format(data_as_date);

            OWLLiteral data_as_literal = dataFactory.getOWLLiteral(data_as_formatteddate, OWL2Datatype.XSD_DATE_TIME);
            dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, ind_subject, data_as_literal);
        } else if ( data instanceof Boolean) {
            Boolean data_as_boolean = (Boolean) data;
            dataPropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, ind_subject, data_as_boolean);
        }

        return dataPropertyAssertionAxiom;
    }

    @Override
    public OWLObjectProperty getObjectProperty(
            String propName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm) {

        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(propName, pm);
        return objectProperty;
    }

    @Override
    public OWLDataProperty getDataProperty(
            String propName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm) {

        OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(propName, pm);
        return dataProperty;
    }

    @Override
    public OWLNamedIndividual getNamedIndividual(
            String indName,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm) {

        OWLNamedIndividual ind = dataFactory.getOWLNamedIndividual(indName, pm);
        return ind;
    }

    @Override
    public boolean isObjectPropEntailed(
            PelletReasoner reasoner,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            OWLNamedIndividual ind_object) {

        OWLObjectPropertyAssertionAxiom objectPropertyAssertionAxiom = getObjectPropertyAssertionAxiom(
                dataFactory, pm, ind_subject, propName, ind_object);

        return reasoner.isEntailed(objectPropertyAssertionAxiom);
    }

    @Override
    public boolean isDataPropEntailed(
            PelletReasoner reasoner,
            OWLDataFactory dataFactory,
            PrefixOWLOntologyFormat pm,
            OWLNamedIndividual ind_subject,
            String propName,
            Object data) {

        OWLDataPropertyAssertionAxiom dataPropertyAssertionAxiom = getDataPropertyAssertionAxiom(dataFactory, pm, ind_subject, propName, data);

        return reasoner.isEntailed(dataPropertyAssertionAxiom);
    }

//    @Override
//    public void createRuleObjectForTest(
//            OWLOntologyManager ontManager,
//            OWLDataFactory factory,
//            OWLOntology myOntology,
//            PrefixOWLOntologyFormat pm
//    ) {
//
//        IRI ontologyIRI = myOntology.getOntologyID().getOntologyIRI();
//
//        //SWRL Variables
//        SWRLVariable context = factory.getSWRLVariable(IRI.create(ontologyIRI + "#context"));
//        SWRLVariable postRequest = factory.getSWRLVariable(IRI.create(ontologyIRI + "#postRequest"));
//        SWRLVariable audience = factory.getSWRLVariable(IRI.create(ontologyIRI + "#audience"));
//        SWRLVariable audienceMember = factory.getSWRLVariable(IRI.create(ontologyIRI + "#audienceMember"));
//        SWRLVariable medium = factory.getSWRLVariable(IRI.create(ontologyIRI + "#medium"));
//        SWRLVariable t = factory.getSWRLVariable(IRI.create(ontologyIRI + "#t"));
//        SWRLVariable location = factory.getSWRLVariable(IRI.create(ontologyIRI + "#location"));
//        SWRLVariable includedPeople = factory.getSWRLVariable(IRI.create(ontologyIRI + "#includedPeople"));
//
//        //SWRL Classes
//        //Contexts
//        OWLClass party = factory.getOWLClass(":Party", pm);
//        OWLClass leisure = factory.getOWLClass(":Leisure", pm);
//        OWLClass beach = factory.getOWLClass(":Beach", pm);
//        OWLClass eatAndDrink = factory.getOWLClass(":EatAndDrink", pm);
//        OWLClass sightseeing = factory.getOWLClass(":Sightseeing", pm);
//        OWLClass meeting = factory.getOWLClass(":Meeting", pm);
//        OWLClass colleagueMeeting = factory.getOWLClass(":ColleagueMeeting", pm);
//        OWLClass friendMeeting = factory.getOWLClass(":FriendMeeting", pm);
//        OWLClass protestMeeting = factory.getOWLClass(":ProtestMeeting", pm);
//        OWLClass researchMeeting = factory.getOWLClass(":ResearchMeeting", pm);
//        OWLClass work = factory.getOWLClass(":Work", pm);
//
//        //Included Locations
//        OWLClass bar = factory.getOWLClass(":Bar", pm);
//        OWLClass cafe = factory.getOWLClass(":Cafe", pm);
//        OWLClass college = factory.getOWLClass(":College", pm);
//        OWLClass museum = factory.getOWLClass(":Museum", pm);
//        OWLClass university = factory.getOWLClass(":University", pm);
//
//        //Date
//        OWLClass date = factory.getOWLClass(":dateTime", pm);
//
//        //SWRL Properties without values
//        OWLObjectProperty hasAudience = factory.getOWLObjectProperty(":hasAudience", pm);
//        OWLObjectProperty hasAudienceMember = factory.getOWLObjectProperty(":hasAudienceMember", pm);
//        OWLObjectProperty hasMedium = factory.getOWLObjectProperty(":hasMedium", pm);
//        OWLObjectProperty isInContext = factory.getOWLObjectProperty(":isInContext", pm);
//        OWLObjectProperty hasDateTake = factory.getOWLObjectProperty(":hasDateTaken", pm);
//        OWLObjectProperty isInLocation = factory.getOWLObjectProperty(":isInLocation", pm);
//        OWLObjectProperty hasIncludedPeople = factory.getOWLObjectProperty(":hasIncludedPeople", pm);
//
//        //DATA lı swrl leri dynamic olarak ekle.
//
//        //for equal t time use built in
//
//
//        //Rejects
//        OWLObjectProperty rejectedIn = factory.getOWLObjectProperty(":rejectedIn", pm);
//        OWLObjectProperty rejectedBecauseOf = factory.getOWLObjectProperty(":rejectedBecauseOf", pm);
//        OWLObjectProperty rejectedBecauseOfDate = factory.getOWLObjectProperty(":rejectedBecauseOfDate", pm);
//
//
//        //Rule u oluştur
//        SWRLClassAtom partyContext = factory.getSWRLClassAtom(party, context);
//        SWRLClassAtom barLocation = factory.getSWRLClassAtom(bar, location);
//        SWRLObjectPropertyAtom hasA =
//                factory.getSWRLObjectPropertyAtom(hasAudience, postRequest, audience);
//        SWRLObjectPropertyAtom hasAu =
//                factory.getSWRLObjectPropertyAtom(hasAudienceMember, audience, audienceMember);
//        Set<SWRLAtom> body = new HashSet<SWRLAtom>();
//        body.add(hasAu);
//        body.add(partyContext);
//        body.add(barLocation);
//        body.add(hasA);
//
//        SWRLObjectPropertyAtom reject1 =
//                factory.getSWRLObjectPropertyAtom(rejectedIn, audience, postRequest);
//        SWRLObjectPropertyAtom reject2 =
//                factory.getSWRLObjectPropertyAtom(rejectedBecauseOf, audience, audienceMember);
//        Set<SWRLAtom> head = new HashSet<SWRLAtom>();
//        head.add(reject1);
//        head.add(reject2);
//
//        SWRLRule rule = factory.getSWRLRule(body, head);
//
//        XStream xstream = new XStream(new DomDriver()); // does not require XPP3 library
//        String xml = xstream.toXML(rule);
//        System.out.println("xml");
//        System.out.println(xml);
//        SWRLRule ruleAgain = (SWRLRule) xstream.fromXML(xml);
//        System.out.println("EVREKA!");
//
//
//        ontManager.applyChange(new AddAxiom(myOntology, ruleAgain));
//        try {
//            ontManager.saveOntology(myOntology);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    // Dilara's method
    //This method is to use OWL API and create rule from our own Rule object.
    @Override
    public SWRLRule getOWLRepresentation(RuleForPrinego rule){

        OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = ontManager.getOWLDataFactory();

        IRI ontologyIRI = IRI.create("http://mas.cmpe.boun.edu.tr/nadin/ontologies/protos");
        PrefixOWLOntologyFormat pm = new PrefixOWLOntologyFormat();


        pm.setDefaultPrefix(ontologyIRI + "#");
        System.out.println(ontologyIRI);
        //SWRL Variables
        SWRLVariable context=factory.getSWRLVariable(IRI.create("urn:swrl"+"#context"));
        SWRLVariable postRequest=factory.getSWRLVariable(IRI.create("urn:swrl"+"#postRequest"));
        SWRLVariable audience=factory.getSWRLVariable(IRI.create("urn:swrl"+"#audience"));
        SWRLVariable audienceMember=factory.getSWRLVariable(IRI.create("urn:swrl"+"#audienceMember"));
        SWRLVariable medium=factory.getSWRLVariable(IRI.create("urn:swrl"+"#medium"));
        SWRLVariable t=factory.getSWRLVariable(IRI.create("urn:swrl"+"#t"));
        SWRLVariable location=factory.getSWRLVariable(IRI.create("urn:swrl"+"#location"));
        SWRLVariable includedPeople=factory.getSWRLVariable(IRI.create("urn:swrl"+"#includedPeople"));

        //SWRL Classes
        //Contexts
        OWLClass party=factory.getOWLClass(":Party",pm);
        OWLClass leisure=factory.getOWLClass(":Leisure",pm);
        OWLClass beach=factory.getOWLClass(":Beach",pm);
        OWLClass eatAndDrink=factory.getOWLClass(":EatAndDrink",pm);
        OWLClass sightseeing=factory.getOWLClass(":Sightseeing",pm);
        OWLClass meeting=factory.getOWLClass(":Meeting",pm);
        OWLClass colleagueMeeting=factory.getOWLClass(":ColleagueMeeting",pm);
        OWLClass friendMeeting=factory.getOWLClass(":FriendMeeting",pm);
        OWLClass protestMeeting=factory.getOWLClass(":ProtestMeeting",pm);
        OWLClass researchMeeting=factory.getOWLClass(":ResearchMeeting",pm);
        OWLClass work=factory.getOWLClass(":Work",pm);

        //For context names in the dropdown list
        Map<String, OWLClass> contextName = new HashMap<String,OWLClass>();
        contextName.put("Party", party);
        contextName.put("Leisure", leisure);
        contextName.put("Beach", beach);
        contextName.put("Eat And Drink", eatAndDrink);
        contextName.put("Sightseeing", sightseeing);
        contextName.put("Meeting", meeting);
        contextName.put("Colleague Meeting", colleagueMeeting);
        contextName.put("Friend Meeting", friendMeeting);
        contextName.put("Protest Meeting", protestMeeting);
        contextName.put("Research Meeting", researchMeeting);
        contextName.put("Work", work);

        //Included Locations
        OWLClass bar=factory.getOWLClass(":Bar",pm);
        OWLClass cafe=factory.getOWLClass(":Cafe",pm);
        OWLClass college=factory.getOWLClass(":College",pm);
        OWLClass museum=factory.getOWLClass(":Museum",pm);
        OWLClass university=factory.getOWLClass(":University",pm);

        //For location names in the dropdown list
        Map<String, OWLClass> locationName = new HashMap<String,OWLClass>();
        locationName.put("Bar", bar);
        locationName.put("Cafe", cafe);
        locationName.put("College", college);
        locationName.put("Museum", museum);
        locationName.put("University", university);

        //Date
        OWLClass dateTime=factory.getOWLClass(":dateTime",pm);

        //SWRL Object Properties without values
        OWLObjectProperty hasAudience = factory.getOWLObjectProperty(":hasAudience",pm);
        OWLObjectProperty hasAudienceMember = factory.getOWLObjectProperty(":hasAudienceMember",pm);
        OWLObjectProperty hasMedium = factory.getOWLObjectProperty(":hasMedium",pm);
        OWLObjectProperty hasDateTaken = factory.getOWLObjectProperty(":hasDateTaken",pm);
        OWLObjectProperty isInContext = factory.getOWLObjectProperty(":isInContext",pm);
        OWLObjectProperty isInLocation = factory.getOWLObjectProperty(":isInLocation",pm);
        OWLObjectProperty hasIncludedPeople = factory.getOWLObjectProperty(":hasIncludedPeople",pm);
        OWLObjectProperty hasIncludedPeopleMember = factory.getOWLObjectProperty(":hasIncludedPeopleMember",pm);

        OWLObjectProperty isFamilyOf = factory.getOWLObjectProperty(":isPartOfFamilyOf",pm);
        OWLObjectProperty isColleagueOf = factory.getOWLObjectProperty(":isColleagueOf",pm);
        OWLObjectProperty isBossOf = factory.getOWLObjectProperty(":isBossOf",pm);
        OWLObjectProperty isFriendAtWork = factory.getOWLObjectProperty(":isFriendAtWork",pm);
        OWLObjectProperty isFriendOf = factory.getOWLObjectProperty(":isFriendOf",pm);
        OWLObjectProperty isCloseFriend = factory.getOWLObjectProperty(":isCloseFriendOf",pm);
        OWLObjectProperty isFriendAtCollege = factory.getOWLObjectProperty(":isFriendAtCollege",pm);
        OWLObjectProperty isOldFriendOf = factory.getOWLObjectProperty(":isOldFriendOf",pm);
        OWLObjectProperty isProblematicFriendOf = factory.getOWLObjectProperty(":isProblematicFriendOf",pm);
        OWLObjectProperty isInRelationshipWith = factory.getOWLObjectProperty(":isInRelationshipWith",pm);

        //For relation names in the dropdown list
        Map<String, OWLObjectProperty> relationName = new HashMap<String,OWLObjectProperty>();
        relationName.put("Family", isFamilyOf);
        relationName.put("Colleague", isColleagueOf);
        relationName.put("Boss", isBossOf);
        relationName.put("FriendAtWork", isFriendAtWork);
        relationName.put("Friend", isFriendOf);
        relationName.put("CloseFriend", isCloseFriend);
        relationName.put("FriendAtCollege", isFriendAtCollege);
        relationName.put("OldFriend", isOldFriendOf);
        relationName.put("ProblematicFriend", isProblematicFriendOf);
        relationName.put("inRelationshipWith", isInRelationshipWith);


        //DATA l˝ swrl leri dynamic olarak ekle.

        //for equal t time use built in

        //Rejects
        OWLObjectProperty rejectedIn = factory.getOWLObjectProperty(":rejectedIn",pm);
        OWLObjectProperty rejectedBecauseOf = factory.getOWLObjectProperty(":rejectedBecauseOf",pm);
        OWLObjectProperty rejectedBecauseOfDate = factory.getOWLObjectProperty(":rejectedBecauseOfDate",pm);
        OWLObjectProperty   rejects = factory.getOWLObjectProperty(":rejects",pm);

        //Individuals
        OWLNamedIndividual alice = factory.getOWLNamedIndividual(":Agent_ALICE", pm);
        OWLNamedIndividual bob = factory.getOWLNamedIndividual(":Agent_BOB", pm);
        OWLNamedIndividual carol = factory.getOWLNamedIndividual(":Agent_CAROL", pm);
        OWLNamedIndividual david = factory.getOWLNamedIndividual(":Agent_DAVID", pm);
        OWLNamedIndividual errol = factory.getOWLNamedIndividual(":Agent_ERROL", pm);
        OWLNamedIndividual filipo = factory.getOWLNamedIndividual(":Agent_FILIPO", pm);

        //to use the individuals as swrl rule arguments
        SWRLIndividualArgument argAlice = factory.getSWRLIndividualArgument(alice);
        SWRLIndividualArgument argBob = factory.getSWRLIndividualArgument(bob);
        SWRLIndividualArgument argCarol = factory.getSWRLIndividualArgument(carol);
        SWRLIndividualArgument argDavid = factory.getSWRLIndividualArgument(david);
        SWRLIndividualArgument argErrol = factory.getSWRLIndividualArgument(errol);
        SWRLIndividualArgument argFilipo = factory.getSWRLIndividualArgument(filipo);
        
    	OWLAnnotationProperty weight = factory.getOWLAnnotationProperty(":weight",pm);


        //For individual names for agents
        Map<String, SWRLIndividualArgument> agentProperName = new HashMap<String,SWRLIndividualArgument>();
        agentProperName.put("ALICE",argAlice);
        agentProperName.put("BOB",argBob);
        agentProperName.put("CAROL",argCarol);
        agentProperName.put("DAVID",argDavid);
        agentProperName.put("ERROL",argErrol);
        agentProperName.put("FILIPO",argFilipo);

        //Rules
        Set<SWRLAtom> body=new HashSet<SWRLAtom>();
        Set<SWRLAtom> head=new HashSet<SWRLAtom>();
        Set<OWLAnnotation> ann=new HashSet<OWLAnnotation>();


        //Then add the body and the head sets according to the rule
        SWRLObjectPropertyAtom  reject1 =
                factory.getSWRLObjectPropertyAtom(rejects, agentProperName.get(rule.getOwnerUser().getUid()),postRequest);

        head.add(reject1);

        if(!rule.getAudience().isEmpty()){

            SWRLObjectPropertyAtom  hasAu =
                    factory.getSWRLObjectPropertyAtom(hasAudience,postRequest,audience);
            body.add(hasAu);


            SWRLObjectPropertyAtom  hasAuMem =
                    factory.getSWRLObjectPropertyAtom(hasAudienceMember,audience, agentProperName.get(rule.getAudience().get(0).getUid()));
            body.add(hasAuMem);

            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,audience,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOf,audience, agentProperName.get(rule.getAudience().get(0).getUid()));
            head.add(reject3);

        }

        if(rule.getAudiGroupName()!=null ){

            SWRLObjectPropertyAtom  hasAu =
                    factory.getSWRLObjectPropertyAtom(hasAudience,postRequest,audience);
            body.add(hasAu);

            SWRLObjectPropertyAtom  hasAuMem =
                    factory.getSWRLObjectPropertyAtom(hasAudienceMember,audience,audienceMember);
            body.add(hasAuMem);

            SWRLObjectPropertyAtom  hasAuMemName =
                    factory.getSWRLObjectPropertyAtom(relationName.get(rule.getAudiGroupName()),audienceMember, agentProperName.get(rule.getOwnerUser().getUid()));
            body.add(hasAuMemName);

            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,audience,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOf,audience,audienceMember);
            head.add(reject3);

        }
        if(rule.getContext()!=null){


            System.out.println("got in to context");
            SWRLClassAtom contextInfo = factory.getSWRLClassAtom(contextName.get(rule.getContext()),context);
            body.add(contextInfo);

            SWRLObjectPropertyAtom  hasMed =
                    factory.getSWRLObjectPropertyAtom(hasMedium,postRequest,medium);
            body.add(hasMed);

            SWRLObjectPropertyAtom  cont =
                    factory.getSWRLObjectPropertyAtom(isInContext,medium,context);
            body.add(cont);

            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,medium,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOf,medium,context);
            head.add(reject3);




        }

        if(rule.isUseCalendar() ){


            SWRLObjectPropertyAtom  hasMed =
                    factory.getSWRLObjectPropertyAtom(hasMedium,postRequest,medium);
            body.add(hasMed);


            SWRLClassAtom dateInfo = factory.getSWRLClassAtom(dateTime,t);
            body.add(dateInfo);



            SWRLDArgument date=
                    factory.getSWRLLiteralArgument(factory.getOWLLiteral(rule.getDate()));
            List<SWRLDArgument> arguments = new ArrayList<SWRLDArgument>();
            arguments.add(t);
            arguments.add(date);

            SWRLBuiltInAtom equal =
                    factory.getSWRLBuiltInAtom(SWRLBuiltInsVocabulary.EQUAL.getIRI(),arguments);

            body.add(equal);


            SWRLObjectPropertyAtom  hasDate =
                    factory.getSWRLObjectPropertyAtom(hasDateTaken,medium,t);
            body.add(hasDate);


            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,medium,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOfDate,medium,t);
            head.add(reject3);
        }

        if(!rule.getIncludedLocations().isEmpty()){

            SWRLClassAtom locationInfo =
                    factory.getSWRLClassAtom(locationName.get(rule.getIncludedLocations().get(0)),location);
            body.add(locationInfo);

            SWRLObjectPropertyAtom  hasMed =
                    factory.getSWRLObjectPropertyAtom(hasMedium,postRequest,medium);
            body.add(hasMed);

            SWRLObjectPropertyAtom  isInLoc =
                    factory.getSWRLObjectPropertyAtom(isInLocation,medium,location);
            body.add(isInLoc);



            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,medium,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOf,medium,context);
            head.add(reject3);

        }

        if(!rule.getIncludedPeople().isEmpty()){

            SWRLObjectPropertyAtom  hasIncPeople =
                    factory.getSWRLObjectPropertyAtom(hasIncludedPeople,postRequest,includedPeople);
            body.add(hasIncPeople);

            SWRLObjectPropertyAtom  hasIncPeopleMem =
                    factory.getSWRLObjectPropertyAtom(hasIncludedPeopleMember,includedPeople, agentProperName.get(rule.getIncludedPeople().get(0).getUid()));
            body.add(hasIncPeopleMem);

            SWRLObjectPropertyAtom  reject2 =
                    factory.getSWRLObjectPropertyAtom(rejectedIn,includedPeople,postRequest);
            head.add(reject2);

            SWRLObjectPropertyAtom  reject3 =
                    factory.getSWRLObjectPropertyAtom(rejectedBecauseOf,includedPeople, agentProperName.get(rule.getIncludedPeople().get(0).getUid()));
            head.add(reject3);

        }

        OWLAnnotation annotation=factory.getOWLAnnotation(weight,factory.getOWLLiteral(rule.getWeight()));
		
        ann.add(annotation);
		
		SWRLRule swrlRule = factory.getSWRLRule(body, head, ann);
		
   
        return swrlRule;
    }

    @Override
    public RuleForPrinego SWRLtoRule(SWRLRule swl){

        RuleForPrinego rule = new RuleForPrinego();
        Iterator<SWRLAtom> it =swl.getBody().iterator();
        while(it.hasNext()){
            SWRLAtom atom = it.next();
            String temp = atom.toString();

            if(temp.contains("location")){
                if(temp.contains("Bar")){
                    rule.setIncludedLocations(new ArrayList<String>(Arrays.asList("Bar")));
                }else if(temp.contains("Cafe")){
                    rule.setIncludedLocations(new ArrayList<String>(Arrays.asList("Cafe")));
                }else if(temp.contains("College")){
                    rule.setIncludedLocations(new ArrayList<String>(Arrays.asList("College")));
                }else if(temp.contains("Museum")){
                    rule.setIncludedLocations(new ArrayList<String>(Arrays.asList("Museum")));
                }else if(temp.contains("University")){
                    rule.setIncludedLocations(new ArrayList<String>(Arrays.asList("University")));
                }
            }else if(temp.contains("context")){
                if(temp.contains("Party")){
                    rule.setContext("Party");
                }else if(temp.contains("Leisure")){
                    rule.setContext("Leisure");
                }else if(temp.contains("Beach")){
                    rule.setContext("Beach");
                }else if(temp.contains("EatAndDrink")){
                    rule.setContext("Eat And Drink");
                }else if(temp.contains("Sightseeing")){
                    rule.setContext("Sightseeing");
                }else if(temp.contains("Meeting")){
                    rule.setContext("Meeting");
                }else if(temp.contains("ColleagueMeeting")){
                    rule.setContext("Colleague Meeting");
                }else if(temp.contains("FriendMeeting")){
                    rule.setContext("Friend Meeting");
                }else if(temp.contains("ProtestMeeting")){
                    rule.setContext("Protest Meeting");
                }else if(temp.contains("ResearchMeeting")){
                    rule.setContext("Research Meeting");
                }else if(temp.contains("Work")){
                    rule.setContext("Work");
                }

            }else if(temp.contains("isPartOfFamilyOf")){
                rule.setAudiGroupName("Family");
            }else if(temp.contains("isColleagueOf")){
                rule.setAudiGroupName("Colleague");
            }else if(temp.contains("isBossOf")){
                rule.setAudiGroupName("Boss");
            }else if(temp.contains("isFriendAtWork")){
                rule.setAudiGroupName("FriendAtWork");
            }else if(temp.contains("isFriendOf")){
                rule.setAudiGroupName("Friend");
            }else if(temp.contains("isCloseFriendOf")){
                rule.setAudiGroupName("CloseFriend");
            }else if(temp.contains("isFriendAtCollege")){
                rule.setAudiGroupName("FriendAtCollege");
            }else if(temp.contains("isOldFriendOf")){
                rule.setAudiGroupName("OldFriend");
            }else if(temp.contains("isProblematicFriendOf")){
                rule.setAudiGroupName("ProblematicFriend");
            }else if(temp.contains("isInRelationshipWith")){
                rule.setAudiGroupName("inRelationshipWith");
            }else if(temp.contains("hasAudienceMember")){
                if(temp.contains("BOB")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("BOB"))));
                }else if(temp.contains("ALICE")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("ALICE"))));
                }else if(temp.contains("CAROL")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("CAROL"))));
                }else if(temp.contains("DAVID")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("DAVID"))));
                }else if(temp.contains("ERROL")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("ERROL"))));
                }else if(temp.contains("FILIPO")){
                    rule.setAudience(new ArrayList<Agent>(Arrays.asList(new Agent("FILIPO"))));
                }
            }else if(temp.contains("hasIncludedPeopleMember")){
                if(temp.contains("BOB")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("BOB"))));
                }else if(temp.contains("ALICE")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("ALICE"))));
                }else if(temp.contains("CAROL")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("CAROL"))));
                }else if(temp.contains("DAVID")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("DAVID"))));
                }else if(temp.contains("ERROL")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("ERROL"))));
                }else if(temp.contains("FILIPO")){
                    rule.setIncludedPeople(new ArrayList<Agent>(Arrays.asList(new Agent("FILIPO"))));
                }
            }else if(temp.contains("BuiltInAtom")){  //this is for date

                String date = temp.substring(119, 130);
                String d =date;
                String[] s = d.split("-");

                String [] t = s[2].split("T");

                s[2] = t[0];


                date = s[0]+"-"+s[1]+"-"+s[2];
                rule.setDate(date+"T00:00:00Z");
                rule.setUseCalendar(true);
            }
        }

        Iterator<SWRLAtom> head =swl.getHead().iterator();
        while(head.hasNext()){
            SWRLAtom atom = head.next();
            String temp = atom.toString();
            if(temp.contains("rejects")){
                if(temp.contains("BOB")){
                    rule.setOwnerUser((new Agent("BOB")));
                }else if(temp.contains("ALICE")){
                    rule.setOwnerUser((new Agent("ALICE")));
                }else if(temp.contains("CAROL")){
                    rule.setOwnerUser((new Agent("CAROL")));
                }else if(temp.contains("DAVID")){
                    rule.setOwnerUser((new Agent("DAVID")));
                }else if(temp.contains("ERROL")){
                    rule.setOwnerUser((new Agent("ERROL")));
                }else if(temp.contains("FILIPO")){
                    rule.setOwnerUser((new Agent("FILIPO")));
                }
            }
        }
        
        Set <OWLAnnotation> ann = swl.getAnnotations();
		Iterator<OWLAnnotation> iter = ann.iterator();
		
		while(iter.hasNext()){
			
			OWLAnnotation annotation = iter.next();
			
			if(annotation.getProperty().toString().contains("weight")){
				
				OWLLiteral lit = (OWLLiteral) annotation.getValue();
				rule.setWeight(Integer.parseInt(lit.getLiteral()));
				
			}
		}

        return rule;


    }

    @Override
    public List<SWRLRule> listRules(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm
    ) {
        List<SWRLRule> rules = new ArrayList<SWRLRule>();
        for (SWRLRule rule : myOntology.getAxioms(AxiomType.SWRL_RULE)) {
            rules.add(rule);
        }
        return rules;
    }

    @Override
    public boolean createRule(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            SWRLRule rule
    ) {
        ontManager.applyChange(new AddAxiom(myOntology, rule));

        try {
            ontManager.saveOntology(myOntology);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteRule(
            OWLOntologyManager ontManager,
            OWLDataFactory dataFactory,
            OWLOntology myOntology,
            PrefixOWLOntologyFormat pm,
            SWRLRule rule
    ) {

        ontManager.applyChange(new RemoveAxiom(myOntology, rule));

        try {
            ontManager.saveOntology(myOntology);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
