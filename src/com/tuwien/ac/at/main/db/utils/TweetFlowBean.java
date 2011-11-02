package com.tuwien.ac.at.main.db.utils;

import java.util.Map;

import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * TweetFlowBean
 * 
 * @author Raunig Stefan
 *
 * 
 * A parsed tweet will be stored and persisted in a SQLite DB, this bean stands as a 
 * simple representation of a Tweetflow 
 */
public class TweetFlowBean {
	
	private Status status = null;
	
	private String identifier = "";
	private String owner = "";
	private String[] mentions = null;
	private URLEntity[] url = null;
	private Map<String, String> variables = null;
	private boolean closedSequence = false;
	private String[] hashTags = null;
	private String parseDate = "";
	private String operation = "";
	
	public TweetFlowBean(){
	}
	
	public TweetFlowBean(Status status, String identifier, String owner, String parseDate){
		this.status = status;
		this.identifier = identifier;
		this.owner = owner;
		this.parseDate = parseDate;
	}

	/**
	 * @return the Twitter Status Object
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the Status object to set
	 */
	public void setStatus(final Status status) {
		this.status = status;
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(final String owner) {
		this.owner = owner;
	}

	/**
	 * @return the mentions
	 */
	public String[] getMentions() {
		return mentions;
	}

	/**
	 * @param mentions the mentions to set
	 */
	public void setMentions(final String[] mentions) {
		this.mentions = mentions;
	}

	/**
	 * @return the url
	 */
	public URLEntity[] getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(final URLEntity[] url) {
		this.url = url;
	}

	/**
	 * @return the variables
	 */
	public Map<String, String> getVariables() {
		return variables;
	}

	/**
	 * @param variables the variables to set
	 */
	public void setVariables(Map<String, String> variables) {
		this.variables = variables;
	}

	/**
	 * @return the closedSequence
	 */
	public boolean isClosedSequence() {
		return closedSequence;
	}

	/**
	 * @param closedSequence the closedSequence to set
	 */
	public void setClosedSequence(final boolean closedSequence) {
		this.closedSequence = closedSequence;
	}

	/**
	 * @return the hashTags
	 */
	public String[] getHashTags() {
		return hashTags;
	}

	/**
	 * @param hashTags the hashTags to set
	 */
	public void setHashTags(String[] hashTags) {
		this.hashTags = hashTags;
	}

	/**
	 * @return the parseDate
	 */
	public String getParseDate() {
		return parseDate;
	}

	/**
	 * @param parseDate the parseDate to set
	 */
	public void setParseDate(String parseDate) {
		this.parseDate = parseDate;
	}

	/**
	 * @return the serviceInformation
	 */
	public String getServiceInformation() {
		return operation;
	}

	/**
	 * @param serviceInformation the serviceInformation to set
	 */
	public void setServiceInformation(String operation) {
		this.operation = operation;
	}
	@Override
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append("ID: "+identifier+", ");
		str.append("Owner: "+owner+", ");
		str.append("Parsed at: "+ parseDate+", ");
		str.append("Operation.Service: "+operation+", ");
		str.append("Closed Sequence: "+isClosedSequence()+", \n");
		str.append("URL's: "+url+", \n");
		str.append("Var: "+variables+", \n");
		str.append("HashTags: "+hashTags);
		return str.toString();
	}
}
