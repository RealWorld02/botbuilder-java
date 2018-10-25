package Microsoft.Bot.Builder.Dialogs;

import java.util.*;

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


/** 
 Basic configuration options supported by all prompts.
*/
public abstract class ActivityPrompt extends Dialog
{
	private static final String PersistedOptions = "options";
	private static final String PersistedState = "state";

	private PromptValidator<Activity> _validator;

	public ActivityPrompt(String dialogId, PromptValidator<Activity> validator)
	{
		super(dialogId);
//C# TO JAVA CONVERTER TODO TASK: Throw expressions are not converted by C# to Java Converter:
//ORIGINAL LINE: _validator = validator ?? throw new ArgumentNullException(nameof(validator));
		_validator = (validator != null) ? validator : throw new NullPointerException("validator");
	}


	@Override
	public Task<DialogTurnResult> BeginDialogAsync(DialogContext dc, Object options)
	{
		return BeginDialogAsync(dc, options, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public override async Task<DialogTurnResult> BeginDialogAsync(DialogContext dc, object options, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	@Override
	public Task<DialogTurnResult> BeginDialogAsync(DialogContext dc, Object options, CancellationToken cancellationToken)
	{
		if (dc == null)
		{
			throw new NullPointerException("dc");
		}

		if (!(options instanceof PromptOptions))
		{
			throw new IndexOutOfBoundsException("options", "Prompt options are required for Prompt dialogs");
		}

		// Ensure prompts have input hint set
		PromptOptions opt = (PromptOptions)options;
		if (opt.getPrompt() != null && tangible.StringHelper.isNullOrEmpty(opt.getPrompt().InputHint))
		{
			opt.getPrompt().InputHint = InputHints.ExpectingInput;
		}

		if (opt.getRetryPrompt() != null && tangible.StringHelper.isNullOrEmpty(opt.getRetryPrompt().InputHint))
		{
			opt.getRetryPrompt().InputHint = InputHints.ExpectingInput;
		}

		// Initialize prompt state
		Map<String, Object> state = dc.getActiveDialog().getState();
		state.put(PersistedOptions, opt);
		state.put(PersistedState, new HashMap<String, Object>());

		// Send initial prompt
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		await OnPromptAsync(dc.getContext(), (Map<String, Object>)state.get(PersistedState), (PromptOptions)state.get(PersistedOptions), cancellationToken).ConfigureAwait(false);
		return Dialog.EndOfTurn;
	}


	@Override
	public Task<DialogTurnResult> ContinueDialogAsync(DialogContext dc)
	{
		return ContinueDialogAsync(dc, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public override async Task<DialogTurnResult> ContinueDialogAsync(DialogContext dc, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	@Override
	public Task<DialogTurnResult> ContinueDialogAsync(DialogContext dc, CancellationToken cancellationToken)
	{
		if (dc == null)
		{
			throw new NullPointerException("dc");
		}

		// Perform base recognition
		Microsoft.Bot.Builder.Dialogs.DialogInstance instance = dc.getActiveDialog();
		Map<String, Object> state = (Map<String, Object>)instance.getState().get(PersistedState);
		PromptOptions options = (PromptOptions)instance.getState().get(PersistedOptions);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		var recognized = await OnRecognizeAsync(dc.getContext(), state, options, cancellationToken).ConfigureAwait(false);

		// Validate the return value
		PromptValidatorContext<Activity> promptContext = new PromptValidatorContext<Activity>(dc.getContext(), recognized, state, options);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		var isValid = await _validator.invoke(promptContext, cancellationToken).ConfigureAwait(false);

		// Return recognized value or re-prompt
		if (isValid)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			return await dc.EndDialogAsync(recognized.Value, cancellationToken).ConfigureAwait(false);
		}
		else
		{
			return Dialog.EndOfTurn;
		}
	}


	@Override
	public Task<DialogTurnResult> ResumeDialogAsync(DialogContext dc, DialogReason reason, Object result)
	{
		return ResumeDialogAsync(dc, reason, result, null);
	}

	@Override
	public Task<DialogTurnResult> ResumeDialogAsync(DialogContext dc, DialogReason reason)
	{
		return ResumeDialogAsync(dc, reason, null, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public override async Task<DialogTurnResult> ResumeDialogAsync(DialogContext dc, DialogReason reason, object result = null, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	@Override
	public Task<DialogTurnResult> ResumeDialogAsync(DialogContext dc, DialogReason reason, Object result, CancellationToken cancellationToken)
	{
		// Prompts are typically leaf nodes on the stack but the dev is free to push other dialogs
		// on top of the stack which will result in the prompt receiving an unexpected call to
		// dialogResume() when the pushed on dialog ends.
		// To avoid the prompt prematurely ending we need to implement this method and
		// simply re-prompt the user.
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		await RepromptDialogAsync(dc.getContext(), dc.getActiveDialog(), cancellationToken).ConfigureAwait(false);
		return Dialog.EndOfTurn;
	}


	@Override
	public Task RepromptDialogAsync(ITurnContext turnContext, DialogInstance instance)
	{
		return RepromptDialogAsync(turnContext, instance, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: public override async Task RepromptDialogAsync(ITurnContext turnContext, DialogInstance instance, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	@Override
	public Task RepromptDialogAsync(ITurnContext turnContext, DialogInstance instance, CancellationToken cancellationToken)
	{
		Map<String, Object> state = (Map<String, Object>)instance.getState().get(PersistedState);
		PromptOptions options = (PromptOptions)instance.getState().get(PersistedOptions);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
		await OnPromptAsync(turnContext, state, options, cancellationToken).ConfigureAwait(false);
	}


	protected Task OnPromptAsync(ITurnContext turnContext, java.util.Map<String, Object> state, PromptOptions options)
	{
		return OnPromptAsync(turnContext, state, options, null);
	}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent in Java to the 'async' keyword:
//ORIGINAL LINE: protected virtual async Task OnPromptAsync(ITurnContext turnContext, IDictionary<string, object> state, PromptOptions options, CancellationToken cancellationToken = default(CancellationToken))
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
	protected Task OnPromptAsync(ITurnContext turnContext, Map<String, Object> state, PromptOptions options, CancellationToken cancellationToken)
	{
		if (turnContext == null)
		{
			throw new NullPointerException("turnContext");
		}

		if (options == null)
		{
			throw new NullPointerException("options");
		}

		if (options.getPrompt() != null)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to 'await' in Java:
			await turnContext.SendActivityAsync(options.getPrompt(), cancellationToken).ConfigureAwait(false);
		}
	}


	protected Task<PromptRecognizerResult<Activity>> OnRecognizeAsync(ITurnContext turnContext, java.util.Map<String, Object> state, PromptOptions options)
	{
		return OnRecognizeAsync(turnContext, state, options, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: protected virtual Task<PromptRecognizerResult<Activity>> OnRecognizeAsync(ITurnContext turnContext, IDictionary<string, object> state, PromptOptions options, CancellationToken cancellationToken = default(CancellationToken))
	protected Task<PromptRecognizerResult<Activity>> OnRecognizeAsync(ITurnContext turnContext, Map<String, Object> state, PromptOptions options, CancellationToken cancellationToken)
	{
		PromptRecognizerResult<Activity> tempVar = new PromptRecognizerResult<Activity>();
		tempVar.setSucceeded(true);
		tempVar.setValue(turnContext.Activity);
		return Task.FromResult(tempVar);
	}
}