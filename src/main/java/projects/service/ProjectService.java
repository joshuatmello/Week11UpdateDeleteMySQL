package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

public class ProjectService {
	
	//This is my service layer that applies business rules. 
	
	//initializes an object of ProjectDao class. 
	private ProjectDao projectDao = new ProjectDao();
	

	/**
	 * This method is called by method createProject() of the I/O layer
	 * which creates a project that is then sent to the initialized 
	 * ProjectDao class object using the addProject() method below. 
	 */
	
	public Project addProject(Project project) {
		return projectDao.insertProject(project);
		
	}


	/**
	 * Returns the results of the method call to the DAO class.
	 * @return
	 */
	public List<Project> fetchAllProjects() {
		return projectDao.fetchAllProjects();
	}


	public Project fetchProjectbyId(Integer projectId) {
//		First used the Optional<Project> to create method in Dao class
//		Then deleted the first portion and replaced it with return. 
		
//		Optional<Project> op= projectDao.fetchProjectId(projectId);
				
		return projectDao.fetchProjectId(projectId).
				orElseThrow(()-> new NoSuchElementException(
						"Project with project ID= " + projectId
						+ " does not exist."));	
								
	}


	public void modifyProjectDetails(Project project) {
		if(!projectDao.modifyProjectDetails(project)) {
			throw new DbException("The project ID=" + project.getProjectId() + 
					"does not exist");
		}		
	}


	public void deleteProject(Integer projectId) {
		if(!projectDao.deleteProject(projectId)) {
			throw new DbException("The project ID=" + projectId + "does not exist.");
			
		}
		
	}
	

}
