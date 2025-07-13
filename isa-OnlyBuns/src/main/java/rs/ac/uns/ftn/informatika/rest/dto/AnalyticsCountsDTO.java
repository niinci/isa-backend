package rs.ac.uns.ftn.informatika.rest.dto;

public class AnalyticsCountsDTO {

    public long weeklyComments;
    public long monthlyComments;
    public long yearlyComments;

    public long weeklyPosts;
    public long monthlyPosts;
    public long yearlyPosts;

    public AnalyticsCountsDTO(long weeklyComments, long monthlyComments, long yearlyComments,
                              long weeklyPosts, long monthlyPosts, long yearlyPosts) {
        this.weeklyComments = weeklyComments;
        this.monthlyComments = monthlyComments;
        this.yearlyComments = yearlyComments;
        this.weeklyPosts = weeklyPosts;
        this.monthlyPosts = monthlyPosts;
        this.yearlyPosts = yearlyPosts;
    }
}
