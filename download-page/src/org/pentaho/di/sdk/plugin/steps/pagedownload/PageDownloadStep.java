/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package org.pentaho.di.sdk.plugin.steps.pagedownload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepInterface.
 * Classes implementing this interface need to:
 * 
 * - initialize the step
 * - execute the row processing logic
 * - dispose of the step 
 * 
 * Please do not create any local fields in a StepInterface class. Store any
 * information related to the processing logic in the supplied step data interface
 * instead.  
 * 
 */

public class PageDownloadStep extends BaseStep implements StepInterface {

	private static Class<?> PKG = PageDownloadStepMeta.class;
	
	private boolean isUrlInputField = false;
	private int urlFieldIdx = 0;
	private PageDownloadStepMeta meta = null;
	private PageDownloadStepData data = null;
	
	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s 				step description
	 * @param stepDataInterface	step data class
	 * @param c					step copy
	 * @param t					transformation description
	 * @param dis				transformation executing
	 */
	public PageDownloadStep(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}
	
	/**
	 * This method is called by PDI during transformation startup. 
	 * 
	 * It should initialize required for step execution. 
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a database,
	 * as wall as obtaining resources, like file handles.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 * 
	 * @return true if initialization completed successfully, false if there was an error preventing the step from working. 
	 *  
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		// Casting to step-specific implementation classes is safe
		PageDownloadStepMeta meta = (PageDownloadStepMeta) smi;
		PageDownloadStepData data = (PageDownloadStepData) sdi;

		return super.init(meta, data);
	}	

	/**
	 * Once the transformation starts executing, the processRow() method is called repeatedly
	 * by PDI for as long as it returns true. To indicate that a step has finished processing rows
	 * this method must call setOutputDone() and return false;
	 * 
	 * Steps which process incoming rows typically call getRow() to read a single row from the
	 * input stream, change or add row content, call putRow() to pass the changed row on 
	 * and return true. If getRow() returns null, no more rows are expected to come in, 
	 * and the processRow() implementation calls setOutputDone() and returns false to
	 * indicate that it is done too.
	 * 
	 * Steps which generate rows typically construct a new row Object[] using a call to
	 * RowDataUtil.allocateRowData(numberOfFields), add row content, and call putRow() to
	 * pass the new row on. Above process may happen in a loop to generate multiple rows,
	 * at the end of which processRow() would call setOutputDone() and return false;
	 * 
	 * @param smi the step meta interface containing the step settings
	 * @param sdi the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		// safely cast the step settings (meta) and runtime info (data) to specific implementations 
		this.meta = (PageDownloadStepMeta) smi;
		this.data = (PageDownloadStepData) sdi;

		// get incoming row, getRow() potentially blocks waiting for more rows, returns null if no more rows expected
		Object[] r = getRow(); 
		
		// if no more rows are expected, indicate step is finished and processRow() should not be called again
		if (r == null){
			logBasic("Number of incoming rows 0");
			setOutputDone();
			return false;
		} else {
			logBasic("Number of incoming rows " + r.length);
		}

		if (first) {
			first = false;
			setup();
			initFieldIndexes();
		}
		
		Object[] outputRow;
		String pageContent = "";
		if(!meta.getGetUrlFromPreviousFields()) {
			pageContent = downloadPageContent(environmentSubstitute(meta.getUrlField()));
			outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 2, environmentSubstitute(meta.getUrlField()));
		} else {
			pageContent = downloadPageContent(environmentSubstitute(r[urlFieldIdx].toString()));
			outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 2, environmentSubstitute(r[urlFieldIdx].toString()));
		}
		
		outputRow = RowDataUtil.addValueData(r, data.outputRowMeta.size() - 1, pageContent);
		
		// put the row to the output row stream
		putRow(data.outputRowMeta, outputRow);

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		// indicate that processRow() should be called again
		return true;
	}
	
	private void setup() {
		// clone the input row structure and place it in our data object
		data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		data.inputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
		// use meta.getFields() to change it, so it reflects the output row structure 
		try {
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);
		} catch (KettleStepException e) {
			e.printStackTrace();
		}
	
		
	}

	private void initFieldIndexes() throws KettleStepException {
	    if ( isUrlInputField ) {
	      Integer idx = getFieldIdx( data.inputRowMeta, environmentSubstitute( meta.getPrevURLField() ) );
	      if ( idx != null ) {
	    	  urlFieldIdx = idx.intValue();
	      } else {
	        throw new KettleStepException( BaseMessages.getString( PKG, "ElasticSearchBulk.Error.NoJsonField" ) );
	      }
	    }
	  }

	/**
	 * This method is called by PDI once the step is done processing. 
	 * 
	 * The dispose() method is the counterpart to init() and should release any resources
	 * acquired for step execution like file handles or database connections.
	 * 
	 * The meta and data implementations passed in can safely be cast
	 * to the step's respective implementations. 
	 * 
	 * It is mandatory that super.dispose() is called to ensure correct behavior.
	 * 
	 * @param smi 	step meta interface implementation, containing the step settings
	 * @param sdi	step data interface implementation, used to store runtime information
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

		// Casting to step-specific implementation classes is safe
		PageDownloadStepMeta meta = (PageDownloadStepMeta) smi;
		PageDownloadStepData data = (PageDownloadStepData) sdi;
		
		super.dispose(meta, data);
	}
	
	// This method get field ID
	private static Integer getFieldIdx(RowMetaInterface rowMeta, String fieldName) {
		if (fieldName == null) {
			return null;
		}

		for (int i = 0; i < rowMeta.size(); i++) {
			String name = rowMeta.getValueMeta(i).getName();
			if (fieldName.equals(name)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Download data from a web page.
	 */
	public String downloadPageContent(String url) {
		logBasic("Reaching URL: " + url);
		BufferedReader br;
		String line;
		StringBuffer content = new StringBuffer();
		try {
				br = new BufferedReader( new InputStreamReader(new URL(url).openStream()));
				
				while( (line = br.readLine()) != null) {
					content.append(line.trim() + "\n");
				}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return content.toString().replaceAll("\r|\n", "");	
	}
	
}
