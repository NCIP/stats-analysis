package gov.nih.nci.caintegrator.studyQueryService.germline;

import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
import gov.nih.nci.caintegrator.domain.finding.variation.snpFrequency.bean.SNPFrequencyFinding;
import gov.nih.nci.caintegrator.domain.study.bean.Population;
import gov.nih.nci.caintegrator.domain.annotation.snp.bean.SNPAnnotation;
import gov.nih.nci.caintegrator.studyQueryService.dto.FindingCriteriaDTO;
import gov.nih.nci.caintegrator.studyQueryService.dto.germline.SNPFrequencyFindingCriteriaDTO;
import gov.nih.nci.caintegrator.util.HQLHelper;
import gov.nih.nci.caintegrator.util.ArithematicOperator;

import java.util.*;
import java.text.MessageFormat;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

/**
 * Author: Ram Bhattaru
 * Date:   Jul 17, 2006
 * Time:   5:52:11 PM
 */
public class SNPFrequencyFindingHandler extends FindingsHandler {

    protected Collection<? extends Finding> getMyFindings(FindingCriteriaDTO critDTO,
                                                Set<String> snpAnnotationIDs,Session session,
                                                int startIndex, int endIndex) {

        List<SNPFrequencyFinding>  snpFrequencyFindings =
                Collections.synchronizedList(new ArrayList<SNPFrequencyFinding>());
        Set<SNPFrequencyFinding>  snpFrequencyFindingsSet = new HashSet<SNPFrequencyFinding>();

        /* if AnnotationCriteria results in no SNPs then 33 no findings */
        if (snpAnnotationIDs != null && snpAnnotationIDs.size() == 0)
            return snpFrequencyFindings;

        final StringBuffer targetHQL = new StringBuffer(
                        " FROM SNPFrequencyFinding "+ TARGET_FINDING_ALIAS +
                        " {0} {1} WHERE {2} {3} ");

        /*  if AnnotationCriteria resulted in some SNPs:  */
        if (snpAnnotationIDs != null && snpAnnotationIDs.size() > 0) {
            ArrayList arrayIDs = new ArrayList(snpAnnotationIDs);
            for (int i = 0; i < arrayIDs.size();) {
                StringBuffer hql = new StringBuffer("").append(targetHQL);
                Collection values = new ArrayList();
                int begIndex = i;
                i += IN_PARAMETERS ;
                int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                Collection<SNPFrequencyFinding> batchFindings = executeAnnotationQueryForFindingSets(
                        critDTO, values, session, hql, startIndex, endIndex);
                // avoid duplicates
                snpFrequencyFindingsSet.addAll(batchFindings);
                //snpFrequencyFindings.addAll(snpFrequencyFindingsSet);
                if (snpFrequencyFindingsSet.size() >= (endIndex - startIndex + 1) ) {
                    snpFrequencyFindings.addAll(snpFrequencyFindingsSet);
                    snpFrequencyFindingsSet = new HashSet<SNPFrequencyFinding>();
                    return snpFrequencyFindings.subList(0, (endIndex - startIndex ));
                }
            }
            /* means each time it never gotten more than 500 results.  So add to final results */
            snpFrequencyFindings.addAll(snpFrequencyFindingsSet);
                
        }
        else { /* means no AnnotationCriteria was specified in the FindingCriteriaDTO  */
            Collection<SNPFrequencyFinding> findings = executeQueryForFindingSets(
                    critDTO, session, targetHQL, startIndex, endIndex);
            snpFrequencyFindings.addAll(findings);
        }

        return snpFrequencyFindings ;
    }

    protected Collection<SNPFrequencyFinding> executeAnnotationQueryForFindingSets(
                    FindingCriteriaDTO critDTO, Collection<String> snpAnnotationIDs,
                    Session session, StringBuffer targetHQL, int start, int end) {

        final HashMap params = new HashMap();
         SNPFrequencyFindingCriteriaDTO findingCritDTO = (SNPFrequencyFindingCriteriaDTO) critDTO;

        /* 1. Include Annotation Criteria in TargetFinding query   */
         StringBuffer snpAnnotJoin = new StringBuffer("");
         StringBuffer snpAnnotCond = new StringBuffer("");

         /* snpAnnotationIDs were aleady handled in sets of IN_PARAMETERS by calling method */
         if (snpAnnotationIDs != null) {
            appendAnnotationCriteriaHQL(snpAnnotationIDs, snpAnnotJoin, snpAnnotCond, params);
         }

         /*  3. Handle population & Study criteria  */
         String populationJoin = "";
         String populationCond = "";
         List<Population> populationList = handlePopulationCriteria(findingCritDTO, session);
         HashSet<Population> pops = new HashSet<Population>();
         pops.addAll(populationList);
         if (populationList.size() > 0) {
            populationJoin = " LEFT JOIN FETCH " + TARGET_FINDING_ALIAS + ".population ";
            populationCond =  TARGET_FINDING_ALIAS + ".population IN (:pops) AND ";
            params.put("pops", pops);
         }

         String hql  = MessageFormat.format(targetHQL.toString(), new Object[] {
                            snpAnnotJoin.toString(), populationJoin, snpAnnotCond.toString(), populationCond });

          StringBuffer formattedTargetHQL = new StringBuffer(hql);
         /*  2. Handle SNPFrequencyFinding Attributes Criteria itself  and populate targetHQL/params */
         addSNPFrequencyFindingAttriuteCrit(findingCritDTO, formattedTargetHQL, params);

         String andRemovedHQL = HQLHelper.removeTrailingToken(new StringBuffer(formattedTargetHQL), "AND");
         String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(andRemovedHQL), "WHERE");
         Query q = session.createQuery(finalHQL);
         HQLHelper.setParamsOnQuery(params, q);

        if (start == -1 || end == -1) {
            // do not use these indexes.  Just retrieve everything
        }
        else { // set the index values
            q.setFirstResult(0); //RAM: 09/22/06 changed back to original before ftp bug
            q.setMaxResults(end - start);
        }
         List<SNPFrequencyFinding> findings = q.list();
         return findings;
    }
    protected Collection<SNPFrequencyFinding> executeQueryForFindingSets(
                    FindingCriteriaDTO critDTO,
                    Session session, StringBuffer targetHQL, int start, int end) {

        final HashMap params = new HashMap();
         SNPFrequencyFindingCriteriaDTO findingCritDTO = (SNPFrequencyFindingCriteriaDTO) critDTO;

        /* 1. Include Annotation Criteria in TargetFinding query   */
         StringBuffer snpAnnotJoin = new StringBuffer("");
         StringBuffer snpAnnotCond = new StringBuffer("");

         /*  2. Handle population & Study criteria  */
         String populationJoin = "";
         String populationCond = "";
         List<Population> populationList = handlePopulationCriteria(findingCritDTO, session);
         HashSet<Population> pops = new HashSet<Population>();
         pops.addAll(populationList);
         if (populationList.size() > 0) {
            populationJoin = " LEFT JOIN FETCH " + TARGET_FINDING_ALIAS + ".population ";
            populationCond =  TARGET_FINDING_ALIAS + ".population IN (:pops) AND ";
            params.put("pops", pops);
         }

         String hql  = MessageFormat.format(targetHQL.toString(), new Object[] {
                            snpAnnotJoin.toString(), populationJoin, snpAnnotCond.toString(), populationCond });

          StringBuffer formattedTargetHQL = new StringBuffer(hql);
         /*  3. Handle SNPFrequencyFinding Attributes Criteria itself  and populate targetHQL/params */
         addSNPFrequencyFindingAttriuteCrit(findingCritDTO, formattedTargetHQL, params);

         String andRemovedHQL = HQLHelper.removeTrailingToken(new StringBuffer(formattedTargetHQL), "AND");
         String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(andRemovedHQL), "WHERE");
         Query q = session.createQuery(finalHQL);
         HQLHelper.setParamsOnQuery(params, q);

        if (start == -1 || end == -1) {
            // do not use these indexes.  Just retrieve everything
        }
        else { // set the index values
            q.setFirstResult(start); //RAM: 09/22/06 changed back to original before ftp bug
            q.setMaxResults(end - start);
        }
         List<SNPFrequencyFinding> findings = q.list();
         return findings;
    }

    private List<Population> handlePopulationCriteria(SNPFrequencyFindingCriteriaDTO findingCritDTO, Session session) {
        String[] populationNames = findingCritDTO.getPopulationNames();
        String studyName = findingCritDTO.getStudyName();
        String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();

        if ((populationNames == null || populationNames.length < 1) &&
                (studyName == null || studyName.length() < 1) &&
                (sponsorIdentifier == null || sponsorIdentifier.length() < 1) ) {
            return new ArrayList<Population>();
        }

        Criteria popCrit = session.createCriteria(Population.class);
        if (populationNames != null && populationNames.length > 0) {
            popCrit.add(Restrictions.in("name", populationNames));
        }
        boolean appendStudy = isAddStudyCriteria(findingCritDTO);
        if (appendStudy) {
            Criteria studyCrit = popCrit.createCriteria("studyCollection");
            addStudyCriteria(findingCritDTO, studyCrit, session);
        }
        List<Population> populationList = popCrit.list();
        return populationList;
    }

    private void addStudyCriteria( SNPFrequencyFindingCriteriaDTO findingCritDTO, Criteria studyCrit, Session session) {
        String studyName = findingCritDTO.getStudyName();
        String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();
        if (studyName != null && studyName.length() > 0) {
            studyCrit.add(Restrictions.eq("name", studyName));
        }
        if (sponsorIdentifier != null && sponsorIdentifier.length() > 0) {
            studyCrit.add(Restrictions.eq("sponsorStudyIdentifier", sponsorIdentifier));
        }
    }
    private boolean isAddStudyCriteria( SNPFrequencyFindingCriteriaDTO findingCritDTO) {
            String studyName = findingCritDTO.getStudyName();
            String sponsorIdentifier = findingCritDTO.getSponsorStudyIdentifier();
            if ((studyName != null && studyName.length() > 0) ||
                    (sponsorIdentifier != null && sponsorIdentifier.length() > 0) )
               return true;

            return false;
    }
    private void addSNPFrequencyFindingAttriuteCrit(SNPFrequencyFindingCriteriaDTO crit, StringBuffer hql, HashMap params) {

        Float hardyWeinbergPValue  = crit.getHardyWeinbergPValue();
        ArithematicOperator hardyWeinbergPValueOperator = crit.getHardyWeinbergPValueOperator();

        Float minorAlleleFreq = crit.getMinorAlleleFrequency();
        ArithematicOperator minorAlleleOperator = crit.getMinorAlleleOperator();

        Double completionRate = crit.getCompletionRate();
        ArithematicOperator completeRateOperator = crit.getCompleteRateOperator();

        Integer heterCount = crit.getHeterozygoteCount();
        Integer missingAlleleCnt = crit.getMissingAlleleCount();
        Integer missingGenotypeCnt = crit.getMissingGenotypeCount();
        String otherAllele = crit.getOtherAllele();
        Integer otherAllelCnt = crit.getOtherAlleleCount();
        Integer otherHomogyoteCnt = crit.getOtherHomogygoteCount();
        String referenceAllele = crit.getReferenceAllele();
        Integer referenceAlleleCnt = crit.getReferenceAlleleCount();
        Integer referenceHomogzoteCnt = crit.getReferenceHomogyzoteCount();


        if (completionRate != null) {
            if (completeRateOperator == null) completeRateOperator = ArithematicOperator.EQ; // default
            String clause =  TARGET_FINDING_ALIAS + ".completionRate {0} :completionRate AND ";
            String condition = HQLHelper.prepareCondition(completeRateOperator);
            String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
            hql.append(formattedClause);
            params.put("completionRate", completionRate);
        }

        if (hardyWeinbergPValue != null) {
            if (hardyWeinbergPValueOperator == null) hardyWeinbergPValueOperator = ArithematicOperator.EQ; // default
            String clause =  TARGET_FINDING_ALIAS + ".hardyWeinbergPValue {0} :hardyWeinbergPValue AND ";
            String condition = HQLHelper.prepareCondition(hardyWeinbergPValueOperator);
            String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
            hql.append(formattedClause);
            params.put("hardyWeinbergPValue", hardyWeinbergPValue);
        }

        if (minorAlleleFreq != null) {
            if (minorAlleleOperator == null) minorAlleleOperator = ArithematicOperator.EQ; // default
            String clause =  TARGET_FINDING_ALIAS + ".minorAlleleFrequency  {0} :minorAlleleFrequency  AND ";
            String condition = HQLHelper.prepareCondition(minorAlleleOperator);
            String formattedClause = MessageFormat.format(clause, new Object[] {condition} );
            hql.append(formattedClause);
            params.put("minorAlleleFrequency", minorAlleleFreq);
        }

        if (heterCount != null) {
           hql.append( TARGET_FINDING_ALIAS + ".heterozygoteCount = :heterCount AND ");
           params.put("heterCount", heterCount);
        }

        if (missingAlleleCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :missingAlleleCnt AND ");
            params.put("missingAlleleCnt", missingAlleleCnt);
        }
        if (missingGenotypeCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :missingGenotypeCnt AND ");
            params.put("missingGenotypeCount", missingGenotypeCnt);
        }
        if (otherAllele != null) {
            hql.append( TARGET_FINDING_ALIAS + ".missingAlleleCount = :otherAllele AND ");
            params.put("otherAllele", otherAllele);
        }
        if (otherAllelCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".otherAlleleCount = :otherAllelCnt AND ");
            params.put("otherAllelCnt", otherAllelCnt);
        }
        if (otherHomogyoteCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".otherHomogygoteCount = :otherHomogyoteCnt AND ");
            params.put("otherHomogyoteCnt", otherHomogyoteCnt);
        }
        if (referenceAllele != null) {
            hql.append( TARGET_FINDING_ALIAS + ".referenceAllele = :referenceAllele AND ");
            params.put("referenceAllele", referenceAllele);
        }
        if (referenceAlleleCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".referenceAlleleCount = :referenceAlleleCnt AND ");
            params.put("referenceAlleleCnt", referenceAlleleCnt);
        }
        if (referenceHomogzoteCnt != null) {
            hql.append( TARGET_FINDING_ALIAS + ".referenceHomogyzoteCount = :referenceHomogzoteCnt AND ");
            params.put("referenceHomogzoteCnt", referenceHomogzoteCnt);
        }

    }
    protected void initializeProxies(Collection<? extends Finding> findings, Session session) {

        /* first initialize SNPAnnotations */
        Collection<String> snpAnnotsIDs = new HashSet<String>();
        Collection<Long> populationIDs = new HashSet<Long>();
        for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
                SNPFrequencyFinding finding =  (SNPFrequencyFinding) iterator.next();
                snpAnnotsIDs.add(finding.getSnpAnnotation().getId());
                populationIDs.add(finding.getPopulation().getId());
        }
        if(snpAnnotsIDs.size() >0) {
              ArrayList arrayIDs = new ArrayList(snpAnnotsIDs);
              for (int i = 0; i < arrayIDs.size();) {
                  Collection values = new ArrayList();
                  int begIndex = i;
                  i += IN_PARAMETERS ;
                  int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
                  values.addAll(arrayIDs.subList(begIndex,  lastIndex));
                  Criteria snpAnnotcrit = session.createCriteria(SNPAnnotation.class).
                  setFetchMode("geneBiomarkerCollection", FetchMode.EAGER).
                  add(Restrictions.in("id", values));
                  snpAnnotcrit.list();
              }
        }

        /* Second initialize Population */
        if (populationIDs.size() > 0) {
            Criteria populationCrit = session.createCriteria(Population.class).
                                add(Restrictions.in("id", populationIDs));
            populationCrit.list();
        }
    }

    protected void sendMyFindings(FindingCriteriaDTO critDTO,
                          Set<String> snpAnnotationIDs,
                         Session session,  List toBePopulated) {

         List<SNPFrequencyFinding>  snpFrequencyFindings =
                  Collections.synchronizedList(new ArrayList<SNPFrequencyFinding>());

          /* if AnnotationCriteria results in no SNPs then return no findings */
          if (snpAnnotationIDs != null && snpAnnotationIDs.size() == 0)
              return;

          final StringBuffer targetHQL = new StringBuffer(
                          " FROM SNPFrequencyFinding "+ TARGET_FINDING_ALIAS +
                          " {0} {1} WHERE {2} {3} ");

          final HashMap params = new HashMap();
          SNPFrequencyFindingCriteriaDTO findingCritDTO = (SNPFrequencyFindingCriteriaDTO) critDTO;

          StringBuffer snpAnnotJoin = new StringBuffer("");
          StringBuffer snpAnnotCond = new StringBuffer("");
          if (snpAnnotationIDs != null) {
             sendFindingsWithAnnotationCriteria(
                     findingCritDTO, snpAnnotationIDs, session, params,
                     targetHQL, snpAnnotJoin, snpAnnotCond, toBePopulated);
          }
          else {    /* snpAnnotationIDs = null */
             sendFindingsWithoutAnnotationCriteria(findingCritDTO, session, params,
                      targetHQL, snpAnnotJoin, snpAnnotCond, toBePopulated);
          }
        return;
    }

    protected List<? extends Finding> getConcreteTypedFindingList() {
        return new ArrayList<SNPFrequencyFinding>();
    }

    protected Set getConcreteTypedFindingSet() {
        return new HashSet<SNPFrequencyFinding>();
    }

    private void sendFindingsWithAnnotationCriteria(SNPFrequencyFindingCriteriaDTO findingCritDTO,
                                                    Collection<String> snpAnnotationIDs,
                                                    Session session, HashMap params,
                                                    StringBuffer targetHQL, StringBuffer snpAnnotJoin,
                                                    StringBuffer snpAnnotCond, List toBePopulated) {

         getConcreteTypedFindingList();
         List  snpFrequencyFindings = getConcreteTypedFindingList();
         ArrayList arrayIDs = new ArrayList(snpAnnotationIDs);
         for (int i = 0; i < arrayIDs.size();) {
              Collection values = new ArrayList();
              int begIndex = i;
              i += IN_PARAMETERS ;
              int lastIndex = (i < arrayIDs.size()) ? i : (arrayIDs.size());
              values.addAll(arrayIDs.subList(begIndex,  lastIndex));

              appendAnnotationCriteriaHQL(values, snpAnnotJoin,snpAnnotCond, params);

              /* send -1 for start & end index to indicate these values not to be included
                 in the final Hibernate Query */
              Collection<? extends Finding> currentFindings =
                           executeQueryForPopulating(findingCritDTO, session, params, targetHQL,
                                                snpAnnotJoin, snpAnnotCond, -1, -1);

              initializeProxies(currentFindings, session);

              /* convert these  currentFindings in to a List for convenience */
              snpFrequencyFindings.addAll(currentFindings );

              while (snpFrequencyFindings.size() >= BATCH_OBJECT_INCREMENT )
                  populateCurrentResultSet(snpFrequencyFindings, toBePopulated);
         }
         /* Now write remaining findings i.e. less than 500 in one call */
         if (snpFrequencyFindings != null)
             populateCurrentResultSet(snpFrequencyFindings, toBePopulated);

         /* Finally after all the results were written, write an empty Object (HashSet of size=0
           to indicate the caller that all results were written */
          populateCurrentResultSet(getConcreteTypedFindingList(), toBePopulated);
     }

    private void sendFindingsWithoutAnnotationCriteria(
                           SNPFrequencyFindingCriteriaDTO findingCritDTO,
                           Session session, HashMap params,
                           StringBuffer targetHQL, StringBuffer snpAnnotJoin,
                           StringBuffer snpAnnotCond, List toBePopulated) {

        Collection findings = null;

        int start = 0;
        int end = BATCH_OBJECT_INCREMENT ;
        Set toBeSent = null;
        do {
            findings =  executeQueryForPopulating(
                      findingCritDTO, session, params, targetHQL,
                                    snpAnnotJoin, snpAnnotCond, start, end);
            initializeProxies(findings, session);

            toBeSent = new HashSet<SNPFrequencyFinding>();
            toBeSent.addAll(findings);
            process(toBePopulated,  toBeSent);
            start += BATCH_OBJECT_INCREMENT;
            end += BATCH_OBJECT_INCREMENT;;

        }  while(findings.size() >= BATCH_OBJECT_INCREMENT );

        /* send empty data object to let the client know that no more results are present */
        process(toBePopulated, getConcreteTypedFindingSet());

    }


    private Collection<SNPFrequencyFinding> executeQueryForPopulating(SNPFrequencyFindingCriteriaDTO findingCritDTO, Session session, HashMap params, StringBuffer targetHQL, StringBuffer snpAnnotJoin, StringBuffer snpAnnotCond, int start, int end) {

        String populationJoin = "";
        String populationCond = "";
        List<Population> populationList = handlePopulationCriteria(findingCritDTO, session);
        if (populationList.size() > 0) {
           populationJoin = " LEFT JOIN FETCH " + TARGET_FINDING_ALIAS + ".population ";
           populationCond =  TARGET_FINDING_ALIAS + ".population IN (:populationList) AND ";
           params.put("populationList", populationList);
        }

        String hql  = MessageFormat.format(targetHQL.toString(), new Object[] {
                           snpAnnotJoin.toString(), populationJoin, snpAnnotCond.toString(), populationCond });

        StringBuffer formattedTargetHQL = new StringBuffer(hql);

        addSNPFrequencyFindingAttriuteCrit(findingCritDTO, formattedTargetHQL, params);

        String andRemovedHQL = HQLHelper.removeTrailingToken(new StringBuffer(formattedTargetHQL), "AND");
        String finalHQL = HQLHelper.removeTrailingToken(new StringBuffer(andRemovedHQL), "WHERE");
        Query q = session.createQuery(finalHQL);
        HQLHelper.setParamsOnQuery(params, q);

        if (start == -1 || end == -1) {
            // do not use these indexes.  Just retrieve everything
        }
        else { // set the index values
            q.setFirstResult(start);
            q.setMaxResults(end - start);
        }
        List<SNPFrequencyFinding> findings = q.list();
        return findings;
    }


}
