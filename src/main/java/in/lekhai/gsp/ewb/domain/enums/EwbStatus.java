package in.lekhai.gsp.ewb.domain.enums;

public enum EwbStatus {
    ACT("Active", in.lekhai.contract.model.EwbStatus.ACTIVE),
    CNL("Cancelled", in.lekhai.contract.model.EwbStatus.REJECTED),
    DIS("Discarded", in.lekhai.contract.model.EwbStatus.REJECTED);
    private final String status;
    private final in.lekhai.contract.model.EwbStatus ewbSummaryStatus;
    EwbStatus(String status,
              in.lekhai.contract.model.EwbStatus ewbSummaryStatus) {
        this.status = status;
        this.ewbSummaryStatus = ewbSummaryStatus;
    }

    public String getStatus() {
        return status;
    }

    public in.lekhai.contract.model.EwbStatus getEwbSummaryStatus() {
        return ewbSummaryStatus;
    }
}
