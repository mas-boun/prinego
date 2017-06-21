package com.prinego.domain.entity.response;

import com.prinego.domain.entity.response.reason.RejectionReason;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by mester on 31/08/14.
 */
@Data
public class Response implements Serializable {

    private String owner;
    private String responseCode;    // Y or N
    private RejectionReason reason;
    private int pointOffer=-1;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Response:" + "\r\n");
        sb.append("\t" + "owner:" + getOwner() + "\r\n");
        sb.append("\t" + "responseCode:" + getResponseCode() + "\r\n");
        if (getReason() != null) {
            sb.append("\t" + "reason:" + "\r\n");
            sb.append("\t\t" + getReason() + "\r\n");
        }
        if(pointOffer != -1){
        	sb.append("\t" + "point offer:" + "\r\n");
            sb.append("\t\t" + getPointOffer() + "\r\n");
        }
        sb.append("]");

        return sb.toString();
    }

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public RejectionReason getReason() {
		return reason;
	}

	public void setReason(RejectionReason reason) {
		this.reason = reason;
	}
	
	public int getPointOffer() {
		return pointOffer;
	}

	public void setPointOffer(int pointOffer) {
		this.pointOffer = pointOffer;
	}

	
}
