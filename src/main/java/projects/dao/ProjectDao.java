package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mysql.cj.x.protobuf.MysqlxSql.StmtExecute;

import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	
	//This is my DAO data layer. Reads and writes to the MySQL database. 
	
	static final private String CATEGORY_TABLE = "category";
	static final private String MATERIAL_TABLE = "material";
	static final private String PROJECT_TABLE = "project";
	static final private String PROJECT_CATEGORY_TABLE = "project_category";
	static final private String STEP_TABLE = "step";
	
	
	/**
	 * This method will save the project details. First a SQL statement is made.
	 * Then a Connection is obtained and transaction started. A PreparedStatement
	 * is needed since we used user input, and it sets the parameter values for
	 * the Project object. Data is saved, transaction committed. 
	 */
	public Project insertProject(Project project) {
	
	//writes the SQL statement that takes and inserts the values from project
	//from the insertProject() method. Uses ?'s as place holders for values. 
		
	// @formatter:off
	String sql = ""
			+ "INSERT INTO " + PROJECT_TABLE + " "
			+ "(project_name, estimated_hours, actual_hours, difficulty, notes) "
			+ "VALUES "
			+ "(?, ?, ?, ?, ?)";
	// @formatter:on
	
	//obtains connection, uses a try-with-resource statement
	try(Connection conn = DbConnection.getConnection()){
		
		//starts transaction, startTransaction() if from DaoBase class
		startTransaction(conn);
		
		//creates PreparedStatement, using method on Connection class 
		//called prepareStatement(). Uses a try-with-resource. 
		//Passes the SQL statement in as the parameter.
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			
			//sets Parameters, uses convenience method setParameter() from DaoBase
			setParameter(stmt, 1, project.getProjectName(), String.class);
			setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
			setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
			setParameter(stmt, 4, project.getDifficulty(), Integer.class);
			setParameter(stmt, 5, project.getNotes(), String.class);
			
			//performs the insert. Do NOT pass in parameters to executeUpdate()
			//or it resets all parameters and gives an obscure error
			stmt.executeUpdate();
			
			//gets the project ID using convenience method from DaoBase
			//from passing in conn and PROJECT_TABLE. 
			Integer projectId= getLastInsertId(conn, PROJECT_TABLE);
			
			//commits the transaction, using DaoBase method 
			commitTransaction(conn);
			
			//saves the data
			project.setProjectId(projectId);	
			
			return project;
		}
		//catches the inner try block, rolls back transaction if error.
		catch(Exception e) {
			rollbackTransaction(conn);
			throw new DbException(e);			
		}
		}
	//Exception that exception from the try-with-resource 
	catch(SQLException e) {
		throw new DbException(e);
		}
		
	}


	/**
	 * Similar to insertProject but uses ResultSet to retrieve project rows
	 */
	public List<Project> fetchAllProjects() {
		
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
		
		try(Connection conn= DbConnection.getConnection()){
			
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				
				try(ResultSet rs = stmt.executeQuery()){
					List<Project> projects = new LinkedList<>();
					
					while(rs.next()) {
						projects.add(extract(rs, Project.class));
						
//						Project project = new Project();
//						
//						project.setActualHours(rs.getBigDecimal("actual_hours"));
//						project.setDifficulty(rs.getObject("difficulty", Integer.class));
//						project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
//						project.setNotes(rs.getString("notes"));
//						project.setProjectId(rs.getObject("project_id", Integer.class));
//						project.setProjectName(rs.getString("project_name"));
//						
//						projects.add(project);
					
					}
					return projects;
				}
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	public Optional<Project> fetchProjectId(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_Id = ?";
		
		try(Connection conn= DbConnection.getConnection()){
			startTransaction(conn);
			
			try{
				Project project = null;
				
				try(PreparedStatement stmt = conn.prepareStatement(sql)){
					//projectId method parameter
					setParameter(stmt, 1, projectId, Integer.class);
					
					try(ResultSet rs = stmt.executeQuery()){
						
						if(rs.next()) {
							project = extract(rs, Project.class);
						}
					}
				}
				
				if(Objects.nonNull(project)) {
					project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
					project.getSteps().addAll(fetchStepsForProject(conn, projectId));
					project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
					
				}
				
				commitTransaction(conn);
				
				return Optional.ofNullable(project);
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}


	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) 
			throws SQLException {
		
		//@formatter:off
		String sql = ""
			+ "SELECT c.* FROM " + CATEGORY_TABLE + " c "
			+ " JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
			+ " WHERE project_id = ?";
		//@formatter:on
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Category> categories = new LinkedList<>();
				
				while(rs.next()) {
					categories.add(extract(rs, Category.class));
				}
				return categories;
			}
		}
		
	}
	
	private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId) 
			throws SQLException {
		
		String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Material> materials = new LinkedList<>();
				
				while(rs.next()) {
					materials.add(extract(rs, Material.class));
				}
				
				return materials;
			}
		}
		
	}
	
	private List<Step> fetchStepsForProject(Connection conn, Integer projectId) 
			throws SQLException {
		
		String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(sql)){
			setParameter(stmt, 1, projectId, Integer.class);
			
			try(ResultSet rs = stmt.executeQuery()){
				List<Step> steps = new LinkedList<>();
				
				while(rs.next()) {
					steps.add(extract(rs, Step.class));
				}
				
				return steps;
			}
			
		}
				

	}


	public boolean modifyProjectDetails(Project project) {
		
		//@formatter:off
		String sql = "UPDATE " + PROJECT_TABLE + " SET "
				+"project_name = ?, estimated_hours = ?, actual_hours = ?, difficulty = ?, notes = ? "
				+"WHERE project_id = ?"
				;
		//@formatter:on
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, project.getProjectName(), String.class);
				setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
				setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
				setParameter(stmt, 4, project.getDifficulty(), Integer.class);
				setParameter(stmt, 5, project.getNotes(), String.class);
				setParameter(stmt, 6, project.getProjectId(), Integer.class);
				
			boolean updated = stmt.executeUpdate() == 1;
			
			commitTransaction(conn);
			
			return updated;
				
			}			
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
		
	}


	public boolean deleteProject(Integer projectId) {
		String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";
		
		try(Connection conn = DbConnection.getConnection()){
			startTransaction(conn);
			
			try(PreparedStatement stmt = conn.prepareStatement(sql)){
				setParameter(stmt, 1, projectId, Integer.class);
				
				boolean deleted = stmt.executeUpdate() == 1;
				
				commitTransaction(conn);
				
				return deleted;
				
			}
			catch(Exception e) {
				rollbackTransaction(conn);
				throw new DbException(e);
			}
		}
		catch(SQLException e) {
			throw new DbException(e);
		}
	}

}
