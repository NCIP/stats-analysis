package gov.nih.nci.caintegrator.studyQueryService.dto.epi;

/**
  * Author: Ram Bhattaru
  * Date:   Apr 06, 2007
  * Time:   5:08:50 PM
**/

public enum MedicalCondition {
	CHRONICBRONCHITIS("Chroninc Bronchitis"),
	EMPHYSEMA("Emphysema"),
	PNEUOMONIA("Pneuomonia"),
	TUBERCULOSIS("Tuberculosis");

    private String value;

    MedicalCondition(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}