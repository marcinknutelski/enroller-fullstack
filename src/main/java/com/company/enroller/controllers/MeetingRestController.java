package com.company.enroller.controllers;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.Collection;

@RestController
@RequestMapping("/api/meetings")
public class MeetingRestController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetings() {
        Collection<Meeting> meetings = meetingService.getAll();
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }

	// jestesmy w meetings przez id
	// w requestBody -- to jest to co przesylamy
	@RequestMapping(value="/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getOneMeeting(@PathVariable("id") long id){
		Meeting meeting = meetingService.findMeeteingById(id);
		if (meeting == null){
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}	

	// ADD
	// POSTMAN
	// POST
	// http://localhost:8080/meetings/
	// {
    //     "id": 3,
    //     "title": "some title3",
    //     "description": "some description3",
    //     "date": "some date3"
    // }
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> addMeeting(@RequestBody Meeting meeting){
		// sprawdzamy czy istnieje meeting
		Meeting findMeeting = meetingService.findMeeteingById(meeting.getId());
		if (findMeeting != null){
			return new ResponseEntity("Unable to create. A meeting with title " +meeting.getTitle() + " already exist.", HttpStatus.CONFLICT);
		}
		// dodanie meetingu
		meetingService.add(meeting);
		return new ResponseEntity<Meeting>(meeting, HttpStatus.CREATED);
	}

	// DELETE
	// http://localhost:8080/meetings/3
	@RequestMapping(value = "{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteMeeting(@PathVariable("id") long id){
		Meeting meeting = meetingService.findMeeteingById(id);
		if(meeting == null){
			return new ResponseEntity("Unable to delete. Meeting does not exist. ", HttpStatus.NOT_FOUND);
		}
		meetingService.delete(meeting);
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}	

	// UPDATE
	// http://localhost:8080/meetings/5
	// {
    //     "title": "Update title8",
    //     "description": "update description8",
    //     "date": "update date8"
    // }	
	@RequestMapping(value = "{id}", method = RequestMethod.PUT)
	public ResponseEntity<?> updateMeeting(@PathVariable("id") long id, @RequestBody Meeting incomingMeeting){

		Meeting meeting = meetingService.findMeeteingById(id);
		if (meeting == null){
			return new ResponseEntity("Unable to update. Meeting does not exist. ", HttpStatus.NOT_FOUND);
		}

		meeting.setDate(incomingMeeting.getDate());
		meeting.setDescription(incomingMeeting.getDescription());
		meeting.setTitle(incomingMeeting.getTitle());
		return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}


	// ADD participant to meeting
	// http://localhost:8080/meetings/2/participants/user2
	@RequestMapping(value = "/{meetingId}/participants/{participantId}", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipantToMeeting(@PathVariable("meetingId") long id,
                                                     @PathVariable("participantId") String participantId) {
        Meeting meeting = meetingService.findMeeteingById(id);
        if (meeting == null) {
            return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
		}

		Participant participant = participantService.findByLogin(participantId);
        if (participant == null) {
            return new ResponseEntity<>("Participant does not exist.", HttpStatus.NOT_FOUND);
        }

        meetingService.addParticipantToMeeting(id, participant);

        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

	// GET participants from meeting
	// http://localhost:8080/meetings/2/participants
    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetingParticipants(@PathVariable("id") long id) {

        Collection<Participant> participants = meetingService.getMeetingParticipants(id);
        if (participants == null) {
            return new ResponseEntity<>("There is no participants for meeting with id=" + id, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }


	// DELETE
	// http://localhost:8080/meetings/2/participants/user2
	@RequestMapping(value = "/{id}/participants/{participantId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeParticipantFromMeeting(@PathVariable("id") long id,
														@PathVariable("participantId") String participantId) {

		Meeting meeting = meetingService.findMeeteingById(id);
		Participant participant = participantService.findByLogin(participantId);
		if (meeting == null) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		if (participant == null) {
			return new ResponseEntity<>("Participant with this login does not exist.", HttpStatus.NOT_FOUND);
		}

		meetingService.kickParticipantFromMeeting(id, participant);
		meetingService.update(meeting);
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
	}
	
	// SORT
	// http://localhost:8080/meetings/sortedByTitle
	@RequestMapping(value="/sortedByTitle", method = RequestMethod.GET)
	public ResponseEntity<?> getMeetingSortedByTitle(){
		Collection<Meeting> meetings = meetingService.getAllSorted();
		return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
	}

	// SEARCH BY
	// http://localhost:8080/meetings/searchByParticipant?id=user3
    @RequestMapping(value = "/searchByParticipant", method = RequestMethod.GET)
    public  ResponseEntity<?> searchMeetingsByParticipant2(@RequestParam String id) {
        Participant participant = participantService.findByLogin(id);
        if (participant == null) {
            return new ResponseEntity<>("Participant with login " + id + " doesn't exist.", HttpStatus.NOT_FOUND);
        }

        Collection<Meeting> meetings = meetingService.searchMeetingsByParticipant(id);
        if (meetings.size() == 0) {
            return new ResponseEntity<>("Meeting does not exist.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }


}
