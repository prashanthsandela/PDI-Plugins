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

package org.pentaho.di.sdk.plugin.steps.getTagContent;

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
import org.pentaho.di.ui.core.widget.LabelComboVar;
import org.pentaho.di.ui.core.widget.LabelTextVar;
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
public class GetTagContentDialog extends BaseStepDialog implements StepDialogInterface {

	/**
	 *	The PKG member is used when looking up internationalized strings.
	 *	The properties file with localized keys is expected to reside in 
	 *	{the package of the class specified}/messages/messages_{locale}.properties   
	 */
	private static Class<?> PKG = GetTagContentMeta.class; // for i18n purposes

	// this is the object the stores the step's settings
	// the dialog reads the settings from it when opening
	// the dialog writes the settings to it when confirmed 
	private GetTagContentMeta meta;

	// text field holding the name of the field to add to the row stream
	private Text wHelloFieldName;
	private LabelComboVar sPageContentFieldName;
	private LabelTextVar xpath;
	private LabelTextVar occuranceNo;
	private LabelTextVar outputField;
	private String[] fieldNames;
	private Button getTextOnlyValue;

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
	public GetTagContentDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		meta = (GetTagContentMeta) in;
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
		
		// ------------------------------------------------------- //
		// SWT code for building the actual settings dialog        //
		// ------------------------------------------------------- //
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		FormData formData = new FormData();

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "GetTagContent.Shell.Title")); 

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
		
		// Output field Name
		outputField = new LabelTextVar(transMeta, shell, BaseMessages.getString( PKG, "GetTagContentStep.outputFieldName" ), BaseMessages.getString( PKG, "GetTagContentStep.outputFieldName.Tooltip" ));
		props.setLook(outputField);
		FormData formData0 = new FormData();
		formData0.left = new FormAttachment(0, 0);
		formData0.right = new FormAttachment(100, 0);
		formData0.top = new FormAttachment(wStepname, margin);
		outputField.setLayoutData(formData0);
		outputField.addModifyListener(lsMod);

		// Get page content from prev step
		sPageContentFieldName = new LabelComboVar(transMeta, shell, 
				BaseMessages.getString( PKG, "GetTagContent.prevStep" ),
		        BaseMessages.getString( PKG, "GetTagContent.prevStep.Tooltip" ) );
		props.setLook(sPageContentFieldName);
		FormData formData1 = new FormData();
		formData1.left = new FormAttachment(0, 0);
		formData1.right = new FormAttachment(100, 0);
		formData1.top = new FormAttachment(outputField, margin);
		sPageContentFieldName.setLayoutData(formData1);
		sPageContentFieldName.addModifyListener(lsMod);
		sPageContentFieldName.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent arg0) {}			
			public void focusGained(FocusEvent arg0) {
				getPreviousFields(sPageContentFieldName);
			}
		});
		getPreviousFields(sPageContentFieldName);
		
		// Set Xpath
		xpath = new LabelTextVar(transMeta, shell, BaseMessages.getString( PKG, "GetTagContentStep.xpath" ), BaseMessages.getString( PKG, "GetTagContentStep.xpath.Tooltip" ));
		props.setLook(xpath);
		FormData formData2 = new FormData();
		formData2.left = new FormAttachment(0, 0);
		formData2.right = new FormAttachment(100, 0);
		formData2.top = new FormAttachment(sPageContentFieldName, margin);
		xpath.setLayoutData(formData2);
		xpath.addModifyListener(lsMod);
		
		// Set output content as get text only Checkbox
		Label getTextOnly = new Label(shell, SWT.RIGHT);
		getTextOnly.setText(BaseMessages.getString(PKG, "GetTagContentStep.getOnlyText")); 
		props.setLook(getTextOnly);
		FormData formData21 = new FormData();
		formData21.left = new FormAttachment(0, 0);
		formData21.right = new FormAttachment(middle, -margin);
		formData21.top = new FormAttachment(xpath, margin);
		getTextOnly.setLayoutData(formData21);
		
		getTextOnlyValue = new Button(shell, SWT.CHECK | SWT.LEFT);
		props.setLook(getTextOnlyValue);
		FormData fdbUrlInputField = new FormData();
		fdbUrlInputField.left = new FormAttachment(middle, 0);
		fdbUrlInputField.right = new FormAttachment(100, 0);
		fdbUrlInputField.top = new FormAttachment(xpath, margin);
		getTextOnlyValue.setLayoutData(fdbUrlInputField);
		getTextOnlyValue.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent arg0) {
				meta.setChanged();
			}			
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		// Set occurance 
		occuranceNo = new LabelTextVar(transMeta, shell, BaseMessages.getString( PKG, "GetTagContentStep.occuranceNo" ), BaseMessages.getString( PKG, "GetTagContentStep.occuranceNo.Tooltip" ));
		props.setLook(occuranceNo);
		FormData formData3 = new FormData();
		formData3.left = new FormAttachment(0, 0);
		formData3.right = new FormAttachment(100, 0);
		formData3.top = new FormAttachment(getTextOnlyValue, margin);
		occuranceNo.setLayoutData(formData3);
		occuranceNo.addModifyListener(lsMod);
		
		
		// OK and cancel buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); 
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); 

		BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, occuranceNo);

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
		sPageContentFieldName.addSelectionListener(lsDef);
		xpath.addSelectionListener(lsDef);
		occuranceNo.addSelectionListener(lsDef);

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
	        if ( r != null ) {
	          fieldNames = r.getFieldNames();
	        }
	      } catch ( KettleException ke ) {
	        new ErrorDialog( shell,
	          BaseMessages.getString( PKG, "GetTagContent.FailedToGetFields.DialogTitle" ),
	          BaseMessages.getString( PKG, "GetTagContent.FailedToGetFields.DialogMessage" ), ke );
	        return new String[0];
	      }
	    }
	    return fieldNames;
	  }

	/**
	 * This helper method puts the step configuration stored in the meta object
	 * and puts it into the dialog controls.
	 */
	private void populateDialog() {
		wStepname.selectAll();
		sPageContentFieldName.setText(meta.getPageContentFieldId() + "");
		xpath.setText(meta.getXpath() + "");
		occuranceNo.setText(meta.getOccuranceNumber() + "");	
		outputField.setText(meta.getOutputField());
		getTextOnlyValue.setSelection(meta.getTextOnly());
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
		meta.setOutputField(outputField.getText());
		meta.setPageContentFieldId(sPageContentFieldName.getText());
		meta.setOccuranceNumber(occuranceNo.getText());
		meta.setXpath(xpath.getText());
		meta.setTextOnly(getTextOnlyValue.getSelection());
		// close the SWT dialog window
		dispose();
	}
}