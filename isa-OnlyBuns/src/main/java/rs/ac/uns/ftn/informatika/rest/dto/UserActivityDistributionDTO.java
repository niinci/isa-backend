package rs.ac.uns.ftn.informatika.rest.dto;

public class UserActivityDistributionDTO {

    public double postMakersPercentage;
    public double commentOnlyPercentage;
    public double inactivePercentage;

    public UserActivityDistributionDTO(double postMakersPercentage, double commentOnlyPercentage, double inactivePercentage) {
        this.postMakersPercentage = postMakersPercentage;
        this.commentOnlyPercentage = commentOnlyPercentage;
        this.inactivePercentage = inactivePercentage;
    }
}
