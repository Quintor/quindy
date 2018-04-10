package nl.quintor.studybits.student.services;

import nl.quintor.studybits.indy.wrapper.IndyPool;
import nl.quintor.studybits.indy.wrapper.IndyWallet;
import nl.quintor.studybits.indy.wrapper.Prover;
import nl.quintor.studybits.student.entities.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class StudentProverService {

    @Autowired
    private MetaWalletService metaWalletService;

    @Autowired
    private IndyPool indyPool;

    private Map<Long, Prover> proverMap = new HashMap<>();

    public void withProverForStudent(Student student, Consumer<Prover> consumer) throws Exception {
        withProverForStudent(student, prover -> {
            consumer.accept(prover);
            return null;
        });
    }

    public <R> R withProverForStudent(Student student, Function<Prover, R> consumer) throws Exception {
        Prover existingProver = proverMap.get(student.getId());
        if (existingProver != null) {
            return consumer.apply(existingProver);
        } else {
            try (IndyWallet wallet = metaWalletService.createIndyWalletFromMetaWallet(student.getMetaWallet())) {
                try (Prover prover = new Prover(student.getUserName(), indyPool, wallet, student.getUserName())) {
                    proverMap.put(student.getId(), prover);
                    R result = consumer.apply(prover);
                    proverMap.remove(student.getId());

                    return result;
                }
            }
        }
    }
}
