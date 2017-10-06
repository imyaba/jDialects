/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package com.github.drinkjava2.jdialects.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.drinkjava2.jdialects.DialectException;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SequenceIdGenerator;
import com.github.drinkjava2.jdialects.id.TableIdGenerator;
import com.github.drinkjava2.jdialects.utils.StrUtils;

/**
 * A TableModel definition represents a platform dependent Database Table, from
 * 1.0.5 this class name changed from "Table" to "TableModel" to avoid naming
 * conflict to JPA's "@Table" annotation
 * 
 * @author Yong Zhu
 * @since 1.0.2
 */
public class TableModel {
	/** The table tableName in database */
	private String tableName;

	/** check constraint for table */
	private String check;

	/** comment for table */
	private String comment;

	/** Optional, map to which POJO class, this is designed for ORM tool only */
	private Class<?> pojoClass;

	/**
	 * Optional, If support engine like MySQL or MariaDB, add engineTail at the
	 * end of "create table..." DDL, usually used to set encode String like "
	 * DEFAULT CHARSET=utf8" for MySQL
	 */
	private String engineTail;

	/** Columns in this table */
	private List<ColumnModel> columns = new ArrayList<ColumnModel>();
  
	/** IdGenerators */
	private List<IdGenerator> idGenerators = new ArrayList<IdGenerator>();
 
	/** Foreign Keys */
	private List<FKeyModel> fkeyConstraints = new ArrayList<FKeyModel>();

	/** Indexes */
	private List<IndexModel> indexConsts = new ArrayList<IndexModel>();

	/** Unique constraints */
	private List<UniqueModel> uniqueConsts = new ArrayList<UniqueModel>();

	public TableModel() {
		super();
	}

	public TableModel(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return a copy of this TableModel
	 */
	public TableModel newCopy() {// NOSONAR
		TableModel tb = new TableModel();
		tb.tableName = this.tableName;
		tb.check = this.check;
		tb.comment = this.comment;
		tb.pojoClass = this.pojoClass;
		tb.engineTail = this.engineTail;
		if (!columns.isEmpty())
			for (ColumnModel item : columns)
				tb.columns.add(item.newCopy()); 

		if (!idGenerators.isEmpty())
			for (IdGenerator item : idGenerators)
				tb.idGenerators.add(item.newCopy());

		if (!fkeyConstraints.isEmpty())
			for (FKeyModel item : fkeyConstraints)
				tb.fkeyConstraints.add(item.newCopy());

		if (!indexConsts.isEmpty())
			for (IndexModel item : indexConsts)
				tb.indexConsts.add(item.newCopy());

		if (!uniqueConsts.isEmpty())
			for (UniqueModel item : uniqueConsts)
				tb.uniqueConsts.add(item.newCopy());
		return tb;
	}

	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGen
	 */
	public void addTableGenerator(TableIdGenerator tableGenerator) {
		//@formatter:off
		DialectException.assureNotNull(tableGenerator);
		DialectException.assureNotEmpty(tableGenerator.getName(), "TableGen name can not be empty");
		idGenerators.add(tableGenerator);  
	}
 
	/**
	 * Add a "create table..." DDL to generate ID, similar like JPA's TableGen 
	 * @param name The name of TableGen Java object itself
	 * @param tableName The name of the table will created in database to generate ID
	 * @param pkColumnName The name of prime key column
	 * @param valueColumnName The name of value column
	 * @param pkColumnValue The value in prime key column
	 * @param initialValue The initial value
	 * @param allocationSize The allocationSize
	 */
	public void tableGenerator(String name, String tableName, String pkColumnName, String valueColumnName,
			String pkColumnValue, Integer initialValue, Integer allocationSize) {
		addTableGenerator(new TableIdGenerator(name, tableName, pkColumnName, valueColumnName, pkColumnValue,
				initialValue, allocationSize));
	}

	/**
	 * Add a sequence definition DDL, note: some dialects do not support sequence
	 * @param name The name of sequence Java object itself
	 * @param sequenceName the name of the sequence will created in database
	 * @param initialValue The initial value
	 * @param allocationSize The allocationSize
	 */
	public void sequenceGenerator(String name, String sequenceName, Integer initialValue, Integer allocationSize) {
		this.addSequenceGenerator(new SequenceIdGenerator(name, sequenceName, initialValue, allocationSize));
	}

	/**
	 * Add a Sequence Generator, note: not all database support sequence
	 */
	public void addSequenceGenerator(SequenceIdGenerator sequence) {
		DialectException.assureNotNull(sequence);
		DialectException.assureNotEmpty(sequence.getSequenceName(), "SequenceGen name can not be empty");
		idGenerators.add( sequence);
	}

	/**
	 *  Add the table check, note: not all database support table check
	 */
	public TableModel check(String check) {
		this.check = check;
		return this;
	}

	/**
	 *  Add the table comment, note: not all database support table comment
	 */
	public TableModel comment(String comment) {
		this.comment = comment;
		return this;
	}
  
	/**
	 * Add a ColumnModel
	 */
	public TableModel addColumn(ColumnModel column) {
		DialectException.assureNotNull(column);
		DialectException.assureNotEmpty(column.getColumnName(), "Column's columnName can not be empty");
		column.setTableModel(this);
		columns.add(column); 
		return this;
	}
	
	/**
	 * Remove a ColumnModel by given columnName 
	 */
	public TableModel removeColumn(String columnName) {
		List<ColumnModel> oldColumns = this.getColumns();
		Iterator<ColumnModel> columnIter = oldColumns.iterator();
		while (columnIter.hasNext())  
			if (columnIter.next().getColumnName().equals(columnName))
				columnIter.remove(); 
		return this;
	}

	
	/** Map to which POJO class, this is designed for ORM tool only*/
	public TableModel pojoClass(Class<?> pojoClass) {
		DialectException.assureNotNull(pojoClass);  
		this.pojoClass=pojoClass;
		return this;
	}
 
	/**
	 * Start add a column definition piece in DDL, detail usage see demo
	 *  
	 * @param columnName
	 * @return the Column object
	 */
	public ColumnModel column(String columnName) {
		DialectException.assureNotEmpty(columnName, "columnName can not be empty"); 
		for (ColumnModel columnModel : columns)  
			if(columnName.equals(columnModel.getColumnName()))
				throw new DialectException("ColumnModel name '"+columnName+"' already existed");
		ColumnModel column = new ColumnModel(columnName);
		addColumn(column);
		return column;
	}

	/**
	 * Return ColumnModel object by columnName, if not found, return null;
	 */
	public ColumnModel getColumn(String columnName) { 
		for (ColumnModel columnModel : columns)  
			if( columnModel.getColumnName()!=null && columnModel.getColumnName().equals(columnName))
				 return columnModel;  
		return null;
	}
	
	/**
	 *  Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyModel fkey() {
		FKeyModel fkey=new FKeyModel();  
		fkey.setTableName(this.tableName);
		this.fkeyConstraints.add(fkey);
		return fkey;
	}
	
	/**
	 *  Start add a foreign key definition in DDL, detail usage see demo
	 */
	public FKeyModel fkey(String fkeyName) {
		FKeyModel fkey=new FKeyModel();  
		fkey.setTableName(this.tableName);
		fkey.setFkeyName(fkeyName);
		this.fkeyConstraints.add(fkey);
		return fkey; 
	}
	
	/**
	 *  Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index( ) {
		IndexModel index=new IndexModel( );  
		this.indexConsts.add(index);
		return index;
	}
	
	/**
	 *  Start add a Index in DDL, detail usage see demo
	 */
	public IndexModel index(String indexName) {
		IndexModel index=new IndexModel();
		index.setName(indexName); 
		this.indexConsts.add(index);  
		return index;
	}
	
	/**
	 *  Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique( ) {
		UniqueModel unique=new UniqueModel( );  
		this.uniqueConsts.add(unique);
		return unique;
	}
	
	/**
	 *  Start add a unique constraint in DDL, detail usage see demo
	 */
	public UniqueModel unique(String uniqueName) {
		UniqueModel unique=new UniqueModel( );  
		unique.setName(uniqueName);
		this.uniqueConsts.add(unique);
		return unique;
	}
 
	/**
	 * If support engine like MySQL or MariaDB, add engineTail at the end of
	 * "create table..." DDL, usually used to set encode String like " DEFAULT CHARSET=utf8" for MySQL
	 */
	public TableModel engineTail(String engineTail) {
		this.engineTail=engineTail;
		return this;
	}
	
	/**
	 * Search and return the IdGenerator in this TableModel by its name
	 */
	public IdGenerator getIdGenerator(String name) {
		if(StrUtils.isEmpty(name))return null;
		for (IdGenerator idGenerator : idGenerators)  
			if(name.equals(idGenerator.getIdGenName()))
					return idGenerator;		 
		return null;
	}
	
	// getter & setter=========================

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

 
	public List<ColumnModel> getColumns() { 
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}  
 
	public List<FKeyModel> getFkeyConstraints() {
		return fkeyConstraints;
	}

	public void setFkeyConstraints(List<FKeyModel> fkeyConstraints) {
		this.fkeyConstraints = fkeyConstraints;
	}

	public String getEngineTail() {
		return engineTail;
	}

	public void setEngineTail(String engineTail) {
		this.engineTail = engineTail;
	}

	public List<IndexModel> getIndexConsts() {
		return indexConsts;
	}

	public void setIndexConsts(List<IndexModel> indexConsts) {
		this.indexConsts = indexConsts;
	}

	public List<UniqueModel> getUniqueConsts() {
		return uniqueConsts;
	}

	public void setUniqueConsts(List<UniqueModel> uniqueConsts) {
		this.uniqueConsts = uniqueConsts;
	}

	public Class<?> getPojoClass() {
		return pojoClass;
	}

	public void setPojoClass(Class<?> pojoClass) {
		this.pojoClass = pojoClass;
	}

	public List<IdGenerator> getIdGenerators() {
		return idGenerators;
	}

	public void setIdGenerators(List<IdGenerator> idGenerators) {
		this.idGenerators = idGenerators;
	} 
	 
}
