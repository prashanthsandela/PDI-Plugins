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

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

/**
 * This class is part of the demo step plug-in implementation.
 * It demonstrates the basics of developing a plug-in step for PDI. 
 * 
 * The demo step adds a new string field to the row stream and sets its
 * value to "Hello World!". The user may select the name of the new field.
 *   
 * This class is the implementation of StepDialogInterface.
 * Classes implementing this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the step's meta object)
 * - write back any changes the user makes to the step's meta object
 * - report whether the user changed any settings when confirming the dialog 
 * 
 */
public class PageDownloadStepDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = PageDownloadStepMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private PageDownloadStepMeta meta;

	// text field holding the name of the field to add to the row stream
	private Text outputFieldName;
	
	private Text urlFieldName;
	
	private Button bUrlInputField;
	
	private LabelComboVar lcvPreviousColumns;
	
	private String[] fieldNames;

	/**
	 * The constructor should simply invoke super() and save the incoming meta
	 * object to a local variable, so it can conveniently read and write settings
	 * from/to it.
	 * 
	 * @param parent 	the SWT shell to open the dialog in
	 * @param in		the meta object holding the step's settings
	 * @param transMeta	transformation description
	 * @param sname		the step name
	 */
	public PageDownloadStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (PageDownloadStepMeta) in;
	}

	/**
	 * This method is called by Spoon when the user opens the settings dialog of the step.
	 * It should open the dialog and return only once the dialog has been closed by the user.
	 * 
	 * If the user confirms the dialog, the meta object (passed in the constructor) must
	 * be updated to reflect the new step settings. The changed flag of the meta object must 
	 * reflect whether the step configuration was changed by the dialog.
	 * 
	 * If the user cancels the dialog, the meta object must not be updated, and its changed flag
	 * must remain unaltered.
	 * 
	 * The open() method must return the name of the step after the user has confirmed the dialog,
	 * or null if the user cancelled the dialog.
	 */
	public String open() {

		// store some convenient SWT variables 
		Shell parent = getParent();
		Display display = parent.getDisplay();

		// SWT code for preparing the dialog
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
		props.setLook(shell);
		setShellImage(shell, meta);
		
		// Save the value of the changed flag on the meta object. If the user cancels
		// the dialog, it will be restored to this saved value.
		// The "changed" variable is inherited from BaseStepDialog
		changed = meta.hasChanged();
		
		// The ModifyListener used on all controls. It will update the meta object to 
		// indicate that changes are being made.
		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				meta.setChanged();
			}
		};
		
		SelectionListener lsSel = new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				logBasic("Wedget Selected");
				meta.setChanged();
				meta.setGetUrlFromPreviousFields(!meta.getGetUrlFromPreviousFields());
			}
			
			public void widgetDefaultSelected(SelectionEvent arg0) {
				meta.setChanged();
				logBasic("Wedget Default Selected");
			}
		};
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "PageDownload.Shell.Title")); 

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); 
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// output field value
		Label wlValName = new Label(shell, SWT.RIGHT);
		wlValName.setText(BaseMessages.getString(PKG, "PageDownload.FieldName.Label")); 
		props.setLook(wlValName);
		FormData fdlValName = new FormData();
		fdlValName.left = new FormAttachment(0, 0);
		fdlValName.right = new FormAttachment(middle, -margin);
		fdlValName.top = new FormAttachment(wStepname, margin);
		wlValName.setLayoutData(fdlValName);

		outputFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(outputFieldName);
		outputFieldName.addModifyListener(lsMod);
		FormData fdValName = new FormData();
		fdValName.left = new FormAttachment(middle, 0);
		fdValName.right = new FormAttachment(100, 0);
		fdValName.top = new FormAttachment(wStepname, margin);
		outputFieldName.setLayoutData(fdValName);
		
		// URL Field
		Label urlValName = new Label(shell, SWT.RIGHT);
		urlValName.setText(BaseMessages.getString(PKG, "PageDownload.urlField.Label")); 
		props.setLook(urlValName);
		FormData urlFormDataValName = new FormData();
		urlFormDataValName.left = new FormAttachment(0, 0);
		urlFormDataValName.right = new FormAttachment(middle, -margin);
		urlFormDataValName.top = new FormAttachment(outputFieldName, margin);
		urlValName.setLayoutData(urlFormDataValName);

		urlFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(urlFieldName);
		urlFieldName.addModifyListener(lsMod);
		FormData urlfdValName = new FormData();
		urlfdValName.left = new FormAttachment(middle, 0);
		urlfdValName.right = new FormAttachment(100, 0);
		urlfdValName.top = new FormAttachment(outputFieldName, margin);
		urlFieldName.setLayoutData(urlfdValName);
		
		//Checkbox
		Label lUrlInputField = new Label(shell, SWT.RIGHT);
		lUrlInputField.setText(BaseMessages.getString(PKG, "PageDownloadStep.PrevStep.Label")); 
		props.setLook(lUrlInputField);
		FormData fdUrlInputField = new FormData();
		fdUrlInputField.left = new FormAttachment(0, 0);
		fdUrlInputField.right = new FormAttachment(middle, -margin);
		fdUrlInputField.top = new FormAttachment(urlFieldName, margin);
		lUrlInputField.setLayoutData(fdUrlInputField);
		
		bUrlInputField = new Button(shell, SWT.CHECK | SWT.LEFT);
		props.setLook(bUrlInputField);
		FormData fdbUrlInputField = new FormData();
		fdbUrlInputField.left = new FormAttachment(middle, 0);
		fdbUrlInputField.right = new FormAttachment(100, 0);
		fdbUrlInputField.top = new FormAttachment(urlFieldName, margin);
		bUrlInputField.setLayoutData(fdbUrlInputField);
		
		// Dropdown for selecting previous field column
		lcvPreviousColumns = new LabelComboVar(transMeta, shell, 
				BaseMessages.getString( PKG, "PageDownloadStep.InputFields.Label" ),
		        BaseMessages.getString( PKG, "PageDownloadStep.InputFields.Tooltip" ) );
		props.setLook(lcvPreviousColumns);
		lcvPreviousColumns.addModifyListener(lsMod);
		FormData fdbPreviousColumn = new FormData();
		fdbPreviousColumn.left = new FormAttachment(0, 0);
		fdbPreviousColumn.right = new FormAttachment(100, 0);
		fdbPreviousColumn.top = new FormAttachment(bUrlInputField, margin);
		lcvPreviousColumns.setLayoutData(fdbPreviousColumn);
		
		lcvPreviousColumns.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent arg0) {
				logBasic("Lost Focus");
			}
			public void focusGained(FocusEvent arg0) {
				logBasic("Gained Focus");
				getPreviousFields( lcvPreviousColumns );				
			}
		});
		
		// Checkbox
//		Label prevStep = new Label(shell, SWT.RIGHT);
//		prevStep.setText(BaseMessages.getString(PKG, "PageDownloadStep.PrevStep.Label"));
//		
//		final Button prevCheckboxStep = new Button( shell, SWT.CHECK | SWT.RIGHT );
//		prevCheckboxStep.setToolTipText( BaseMessages.getString( PKG, "PageDownloadStep.PrevStep.Tooltip" ) );
//
//		final LabelComboVar selectInputField = new LabelComboVar( transMeta, shell,
//		        BaseMessages.getString( PKG, "PageDownloadStep.InputFields.Label" ),
//		        BaseMessages.getString( PKG, "PageDownloadStep.InputFields.Tooltip" ) );
//		selectInputField.getComboWidget().setEditable( true );
//		props.setLook( selectInputField );
//		selectInputField.addModifyListener( lsMod );
//		selectInputField.addFocusListener( new FocusListener() {
//		public void focusLost( org.eclipse.swt.events.FocusEvent e ) {}
//
//		public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
//			getPreviousFields( selectInputField );
//		}
//		});
//	    getPreviousFields( selectInputField );
//	    selectInputField.setEnabled( prevCheckboxStep.getSelection() );
//		
//		prevCheckboxStep.addSelectionListener(new SelectionListener() {
//			
//			public void widgetSelected(SelectionEvent arg0) {
//				selectInputField.setEnabled(prevCheckboxStep.getSelection());
//			}
//			
//			public void widgetDefaultSelected(SelectionEvent arg0) {
//				widgetSelected(arg0);
//			}
//		});
		      
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, lcvPreviousColumns);

		// Add listeners for cancel and OK
		lsCancel = new Listener() {
			public void handleEvent(Event e) {cancel();}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {ok();}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(SWT.Selection, lsOK);

		// default listener (for hitting "enter")
		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {ok();}
		};
		wStepname.addSelectionListener(lsDef);
		outputFieldName.addSelectionListener(lsDef);
		urlFieldName.addSelectionListener(lsDef);
		bUrlInputField.addSelectionListener(lsSel);
		

		// Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {cancel();}
		});
		
		// Set/Restore the dialog size based on last position on screen
		// The setSize() method is inherited from BaseStepDialog
		setSize();

		// populate the dialog with the values from the meta object
		populateDialog();
		
		// restore the changed flag to original value, as the modify listeners fire during dialog population 
		meta.setChanged(changed);

		// open dialog and enter event loop 
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// at this point the dialog has closed, so either ok() or cancel() have been executed
		// The "stepname" variable is inherited from BaseStepDialog
		return stepname;
	}
	
	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		outputFieldName.setText(meta.getOutputField());	
		urlFieldName.setText(meta.getUrlField());
		bUrlInputField.setSelection(meta.getGetUrlFromPreviousFields());
	}

	private void getPreviousFields( LabelComboVar combo ) {
	    String value = combo.getText();
	    combo.removeAll();
	    combo.setItems( getInputFieldNames() );
	    if ( value != null ) {
	      combo.setText( value );
	    }
	  }

	private String[] getInputFieldNames() {
	    if ( this.fieldNames == null ) {
	      try {
	        RowMetaInterface r = transMeta.getPrevStepFields( stepname );
	        logBasic("Get Input Field names: " + Arrays.toString(r.getFieldNames()));
	        if ( r != null ) {
	          fieldNames = r.getFieldNames();
	        }
	      } catch ( KettleException ke ) {
	        new ErrorDialog( shell,
	          BaseMessages.getString( PKG, "PageDownloadStep.FailedToGetFields.DialogTitle" ),
	          BaseMessages.getString( PKG, "PageDownloadStep.FailedToGetFields.DialogMessage" ), ke );
	        return new String[0];
	      }
	    }

	    logBasic("Get Input Field: " + Arrays.toString(this.fieldNames));
	    return fieldNames;
	  }
	
	/**
	 * Called when the user cancels the dialog.  
	 */
	private void cancel() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to null to indicate that dialog was cancelled.
		stepname = null;
		// Restoring original "changed" flag on the met aobject
		meta.setChanged(changed);
		// close the SWT dialog window
		dispose();
	}
	
	/**
	 * Called when the user confirms the dialog
	 */
	private void ok() {
		// The "stepname" variable will be the return value for the open() method. 
		// Setting to step name from the dialog control
		stepname = wStepname.getText(); 
		// Setting the  settings to the meta object
		meta.setOutputField(outputFieldName.getText());
		
		meta.setUrlField(urlFieldName.getText());
		
		// close the SWT dialog window
		dispose();
	}
}
