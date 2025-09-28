package saveToXml;

import dto.InstructionDTO;
import dto.ProgramDTO;
import generatedFromXml.SProgram;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

import java.io.File;
import java.util.List;

public class XmlProgramSaver {

//    public void save(File file, ProgramDTO programDTO) throws JAXBException {
    public void save(File file, String programName, List<InstructionDTO> instructionDTOList) throws JAXBException {
        SProgram sProgram = XmlProgramReverseMapper.map(programName, instructionDTOList);
        JAXBContext ctx = JAXBContext.newInstance("generatedFromXml");
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(sProgram, file);
    }
}
