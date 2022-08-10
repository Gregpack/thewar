package ru.gregpack.thewar.engine.logic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.mapstruct.factory.Mappers;
import ru.gregpack.thewar.network.messages.dto.mapping.GameStateMapper;
import ru.gregpack.thewar.model.repositories.BarrackInMemoryRepository;
import ru.gregpack.thewar.model.repositories.UnitInMemoryRepository;
import ru.gregpack.thewar.utils.PropertyUtil;

public class LogicModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(GameLogic.class).to(GameLogicImpl.class);
    }

    @Provides
    @Singleton
    public ObjectMapper getObjectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET,false);
        ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        objectMapper.configure(SerializationFeature.CLOSE_CLOSEABLE, false);
        return objectMapper;
    }

    @Provides
    @Inject
    public GameStateMapper gameStateMapper(BarrackInMemoryRepository barrackInMemoryRepository,
                                           UnitInMemoryRepository unitInMemoryRepository) {
        GameStateMapper mapper = Mappers.getMapper(GameStateMapper.class);
        mapper.setBarrackRepository(barrackInMemoryRepository);
        mapper.setUnitRepository(unitInMemoryRepository);
        return mapper;
    }
}
