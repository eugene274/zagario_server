package accountservice.model.response.ApiErrors;

import accountservice.model.response.ApiRequestError;

/**
 * Created by eugene on 10/25/16.
 */
public class PolicyViolationError extends ApiRequestError {
    public PolicyViolationError() {
        super("Credentials policy violation", 3);
    }
}
