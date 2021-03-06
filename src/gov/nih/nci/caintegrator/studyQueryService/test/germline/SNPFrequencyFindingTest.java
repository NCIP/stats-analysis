/*L
 *  Copyright SAIC
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/stats-analysis/LICENSE.txt for details.
 */

package gov.nih.nci.caintegrator.studyQueryService.test.germline;

import gov.nih.nci.caintegrator.domain.finding.bean.Finding;
import gov.nih.nci.caintegrator.domain.finding.variation.snpFrequency.bean.SNPFrequencyFinding;
import gov.nih.nci.caintegrator.domain.study.bean.Population;
import gov.nih.nci.caintegrator.domain.annotation.snp.bean.SNPAnnotation;
import gov.nih.nci.caintegrator.domain.annotation.gene.bean.GeneBiomarker;
import gov.nih.nci.caintegrator.domain.analysis.snp.bean.SNPAssociationFinding;
import gov.nih.nci.caintegrator.studyQueryService.dto.annotation.AnnotationCriteria;
import gov.nih.nci.caintegrator.studyQueryService.dto.germline.SNPFrequencyFindingCriteriaDTO;
import gov.nih.nci.caintegrator.studyQueryService.dto.study.StudyParticipantCriteria;
import gov.nih.nci.caintegrator.studyQueryService.dto.study.PopulationCriteria;
import gov.nih.nci.caintegrator.studyQueryService.germline.FindingsManager;
import gov.nih.nci.caintegrator.util.ArithematicOperator;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Author: Ram Bhattaru
 * Date:   Jul 19, 2006
 * Time:   6:49:07 PM
 */
public class SNPFrequencyFindingTest extends CGEMSTest {
    private SNPFrequencyFindingCriteriaDTO  freqDTO;
     protected StudyParticipantCriteria spCrit;
    public void setUp() throws Exception {
        super.setUp();
        freqDTO = (SNPFrequencyFindingCriteriaDTO) ctx.getBean("snpFrequencyFindingsCriteria");
        freqDTO.setAnnotationCriteria(annotCrit);
    }

    protected void setUpPopulationCriteria() {
        Collection<String> names = new ArrayList<String>();
        //names.add("CASE_ADVANCED");
        names.add("CASE_EARLY");
        PopulationCriteria popCrit = new PopulationCriteria(names);
        spCrit.setPopulationCriteria(popCrit);     
     }

  /*   public void testSNPAnnotationRetrieval() {
        setUpSNPPhysicalPositionCrit();
        setUpPanelCrit();
        try {
            Collection<SNPAnnotation> snpAnnotObjs = manager.getSNPAnnotations(annotCrit);
            for (Iterator<SNPAnnotation> iterator = snpAnnotObjs.iterator(); iterator.hasNext();) {
                SNPAnnotation snpAnnotation = iterator.next();
                System.out.println("SNP_ANNOT_ID: " + snpAnnotation.getId());
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }*/

    public void testSNPFrequencyFindingCriteriaDTO() {
        // 1. setup Annotation Criteria
       //setUpSNPPhysicalPositionCrit();
       //setUpDBSnpCrit();

      setUpGeneBiomarkerCrit();
      //setUpPanelCrit();


        //freqDTO.setMinorAlleleFrequency(new Float(1.0), ArithematicOperator.GE);
        freqDTO.setPopulationNames(new String[] {"CASE"});

       // freqDTO.setCompletionRate(new Double(0.9), ArithematicOperator.LT);
      // freqDTO.setHardyWeinbergPValue(new Float(0.001), ArithematicOperator.LT);

        // Now set up study name criteria
        studyCrit.setName("CGEMS Prostate Cancer WGAS Phase 1");
        freqDTO.setStudyCriteria(studyCrit);

        executeSearch(0, 100);
   }

    public void testPopulateFindings() {
        //setUpSNPPhysicalPositionCrit();
        //setUpPanelCrit();
        //freqDTO.setPopulationNames(new String[] {"CEPH"});
        freqDTO.setHardyWeinbergPValue(new Float(0.001), ArithematicOperator.LT);

        studyCrit.setName("CGEMS Prostate Cancer WGAS Phase 1");
        //freqDTO.setStudyCriteria(studyCrit);
        //setUpGeneBiomarkerCrit();
        //setSNPFindingCriteria();
        try {
             HashSet<SNPFrequencyFinding> actualBatchFindings = null;
             final List findingsToBePopulated =  Collections.synchronizedList(new ArrayList());
             new Thread(new Runnable() {
                 public void run() {
                     try {
                        manager.populateFindings(freqDTO, findingsToBePopulated);
                     } catch(Throwable t) {
                         t.printStackTrace();
                         System.out.println("Error from FindingsManager.populateFindings call: ");
                     }
                 }
                       }
            ).start();

            int count = 1;
            int noOfResults = 0;
            do {
                synchronized(findingsToBePopulated) {
                    if (findingsToBePopulated.size() > 0) {
                         actualBatchFindings  = (HashSet<SNPFrequencyFinding>)findingsToBePopulated.remove(0);
                         for (Iterator<SNPFrequencyFinding> iterator = actualBatchFindings.iterator(); iterator.hasNext();) {
                             SNPFrequencyFinding sf = iterator.next();
                             System.out.print("ID: " + sf.getId());
                             System.out.print("  HardyWeinbergPValue" + sf.getHardyWeinbergPValue()) ;
                         }
                         noOfResults += actualBatchFindings.size();
                         System.out.println("WRITTEN BATCH: " + count++ + " SIZE: " +
                                                          actualBatchFindings.size() + "\n\n");
                         if (actualBatchFindings.size() == 0) {
                            /* means no more to results are coming.  Finished */
                             break;
                         }
                     }
                 }
                 Thread.currentThread().sleep(10);
                for (Iterator iterator = findingsToBePopulated.iterator(); iterator.hasNext();) {
                    Object toBeGCed = iterator.next();
                    toBeGCed = null;
                }
                actualBatchFindings = null;
             }  while(true);

            System.out.println("ALL RESULTS WERE RECEIVED TOTAL: " + noOfResults);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Collection executeSearch(int startIndex, int endIndex) {
           try {
               Collection findings = manager.getFindings(freqDTO, startIndex, endIndex);
               System.out.println("RESULTS COUNT: " + findings.size());
               for (Iterator<? extends Finding> iterator = findings.iterator(); iterator.hasNext();) {
                   SNPFrequencyFinding finding =  (SNPFrequencyFinding) iterator.next();
                   System.out.println("Completion Rate: " + finding.getCompletionRate());
                   System.out.println("HardyWeinbergPValue: " + finding.getHardyWeinbergPValue());
                   System.out.println("HeterozygoteCount: " + finding.getHeterozygoteCount());
                   System.out.println("MissingAlleleCount: " + finding.getMissingAlleleCount());
                   System.out.println("OtherAllele: " + finding.getOtherAllele());
                   System.out.println("OtherAlleleCount: " + finding.getOtherAlleleCount());
                   System.out.println("MissingGenotypeCount: " + finding.getMissingGenotypeCount());
                   System.out.println("MinorAlleleFrequency : " + finding.getMinorAlleleFrequency());
                   printSNPAnnotation(finding.getSnpAnnotation());
                   printPopulation(finding.getPopulation());
               }
               return findings;
              } catch (Throwable t)  {
               System.out.println("CGEMS Exception: ");
               t.printStackTrace();
              }
        return null;
    }
    protected void printSNPAnnotation(SNPAnnotation annot) {
        super.printSNPAnnotation(annot);
        Collection<GeneBiomarker> c = annot.getGeneBiomarkerCollection();
        for (Iterator<GeneBiomarker> iterator = c.iterator(); iterator.hasNext();) {
            GeneBiomarker geneBiomarker =  iterator.next();
            System.out.println("Biomarker: " + geneBiomarker.getHugoGeneSymbol());
        }
    }

    protected void printPopulation(Population p) {
        System.out.println("Population (SHOULD BE NO SQL): ");
        System.out.println("        " + p);
    }
    public static Test suite() {
        TestSuite suit =  new TestSuite();
        suit.addTest(new TestSuite(SNPFrequencyFindingTest.class));
        return suit;
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());

    }

}
