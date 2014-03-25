package com.dgex.offspring.swt.wizard;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IGenericTransactionField {

  /**
   * Returns the label for the field as string
   * 
   * @return
   */
  public String getLabel();

  /**
   * Returns the value for this field.
   * 
   * @return
   */
  public Object getValue();

  /**
   * Creates the Control that is shown next to the label, users can normally
   * enter a value in a control. This control is shown in the editable
   * WizardPage.
   * 
   * @return
   */
  public Control createControl(Composite parent);

  /**
   * Creates the Control that is shown in the readonly wizard page
   * 
   * @return
   */
  public Control createReadonlyControl(Composite parent);

  /**
   * Verify the value entered by the user in the control. The String array can
   * be used to return a message to be displayed in the Decoration.
   * 
   * @return
   */
  public boolean verify(String[] message);
}
