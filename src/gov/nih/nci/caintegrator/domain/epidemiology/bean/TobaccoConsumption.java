/*L
 *  Copyright SAIC
 *
 *  Distributed under the OSI-approved BSD 3-Clause License.
 *  See http://ncip.github.com/stats-analysis/LICENSE.txt for details.
 */

package gov.nih.nci.caintegrator.domain.epidemiology.bean;

import gov.nih.nci.caintegrator.studyQueryService.dto.epi.SmokingStatus;

import java.util.Collection;

public class TobaccoConsumption {

    private Long id;
    private String tobaccoType;
    private String smokingStatus;
    private Double intensity;
    private Integer duration;
    private Integer ageAtInitiation;
    private Integer yearsSinceQuitting;
    
    public Integer getAgeAtInitiation() {
        return ageAtInitiation;
    }
    public void setAgeAtInitiation(Integer ageAtInitiation) {
        this.ageAtInitiation = ageAtInitiation;
    }
    public Integer getDuration() {
        return duration;
    }
    public void setDuration(Integer duration) {
        this.duration = duration;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Double getIntensity() {
        return intensity;
    }

    public void setIntensity(Double intensity) {
        this.intensity = intensity;
    }

    public String getSmokingStatus() {
        return smokingStatus;
    }
    public void setSmokingStatus(String smokingStatus) {
        this.smokingStatus = smokingStatus;
    }
    public String getTobaccoType() {
        return tobaccoType;
    }
    public void setTobaccoType(String tobaccoType) {
        this.tobaccoType = tobaccoType;
    }
    public Integer getYearsSinceQuitting() {
        return yearsSinceQuitting;
    }
    public void setYearsSinceQuitting(Integer yearsSinceQuitting) {
        this.yearsSinceQuitting = yearsSinceQuitting;
    }
    
}
