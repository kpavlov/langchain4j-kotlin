package me.kpavlov.langchain4j.kotlin.samples;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.mock.ChatModelMock;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.UserName;
import dev.langchain4j.service.V;
import me.kpavlov.langchain4j.kotlin.service.TemplateSystemMessageProvider;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ServiceWithTemplateSourceJavaExample {

  // Use for demo purposes
  private static final ChatModel model = new ChatModelMock("Hello");
  private static final Logger LOGGER = getLogger(ServiceWithTemplateSourceJavaExample.class);

  private static final String PROMPT_TEMPLATE_PATH = "prompts/ServiceWithTemplateSourceJavaExample";

  private interface Assistant {
    @UserMessage(PROMPT_TEMPLATE_PATH + "/default-user-prompt.mustache")
    String askQuestion(
      @UserName String userName,
      @V("message") String question
    );
  }

  public static void main(String[] args) {

    final var systemMessageProvider = new TemplateSystemMessageProvider(
      PROMPT_TEMPLATE_PATH + "/default-system-prompt.mustache"
    );

    final Assistant assistant = AiServices.builder(Assistant.class)
      .systemMessageProvider(systemMessageProvider)
      .chatModel(model)
      .build();

    String response = assistant.askQuestion("My friend", "How are you?");
    LOGGER.info("AI Response: {}", response);
  }
}


