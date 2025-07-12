package rs.ac.uns.ftn.informatika.rest.dto;

public class AnalyticsCountsDTO {

    public int weeklyComments;
    public int monthlyComments;
    public int yearlyComments;

    public int weeklyPosts;
    public int monthlyPosts;
    public int yearlyPosts;

    public AnalyticsCountsDTO(int weeklyComments, int monthlyComments, int yearlyComments,
                              int weeklyPosts, int monthlyPosts, int yearlyPosts) {
        this.weeklyComments = weeklyComments;
        this.monthlyComments = monthlyComments;
        this.yearlyComments = yearlyComments;
        this.weeklyPosts = weeklyPosts;
        this.monthlyPosts = monthlyPosts;
        this.yearlyPosts = yearlyPosts;
    }
}
