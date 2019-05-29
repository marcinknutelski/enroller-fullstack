package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.List;
import java.util.Comparator;



@Component("meetingService")
public class MeetingService {

    DatabaseConnector connector;

    @Autowired
    PasswordEncoder passwordEncoder;

    public MeetingService() {
        connector = DatabaseConnector.getInstance();
    }

	public Collection<Meeting> getAll() {
		String hql = "FROM Meeting";
		Query query = connector.getSession().createQuery(hql);
		return query.list();
	}

	public Meeting findMeeteingById(long id){
		return (Meeting) connector.getSession().get(Meeting.class, id);
	}

    public Meeting getMeetingByTitle(String title) {
        Criteria criteria = connector.getSession().createCriteria(Meeting.class);
        return (Meeting) criteria.add(Restrictions.eq("title", title)).uniqueResult();
    }	

	public void add(Meeting meeting){
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().save(meeting);
		transaction.commit();
	}

	public void delete(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().delete(meeting);
		transaction.commit();
		
	}

	public void update(Meeting meeting) {
		Transaction transaction = connector.getSession().beginTransaction();
		connector.getSession().update(meeting);
		transaction.commit();		
	}

    public void addParticipantToMeeting(long id, Participant participant) {
	    Transaction transaction = connector.getSession().beginTransaction();
	    Meeting meeting = this.findMeeteingById(id);
	    meeting.addParticipant(participant);
	    connector.getSession().save(meeting);
	    connector.getSession().save(participant);
	    transaction.commit();
    }

	public Collection<Participant> getMeetingParticipants(long id) {
	    Meeting meeting = this.findMeeteingById(id);
	    return meeting.getParticipants();
    }

	public void kickParticipantFromMeeting(long id, Participant participant){
		Transaction transaction = connector.getSession().beginTransaction();
	    Meeting meeting = this.findMeeteingById(id);
	    meeting.removeParticipant(participant);
	    connector.getSession().save(meeting);
	    connector.getSession().save(participant);
	    transaction.commit();
	}	

	public Collection<Meeting> sortByTitle() {
        List<Meeting> meetings = connector.getSession().createCriteria(Meeting.class).list();
        meetings.sort(Comparator.comparing(Meeting::getTitle, String.CASE_INSENSITIVE_ORDER));
        return meetings;
	}
	
	public Collection<Meeting> getAllSorted() {
		String hql = "FROM Meeting ORDER BY LOWER(title) ASC";
		Query query = connector.getSession().createQuery(hql);
		return query.list();
	}

    public Collection<Meeting> searchMeetingsByParticipant(String login) {
	    Transaction transaction = connector.getSession().beginTransaction();
        String hql = "SELECT m FROM Meeting m JOIN m.participants p WHERE p.login LIKE ?";
        Query query = connector.getSession().createQuery(hql);
        query.setParameter(0, login);
        transaction.commit();
        return query.list();
    }
}
