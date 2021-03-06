package it.unibz.inf.ontop.datalog.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.node.DataNode;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;

import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.castIntoGroundTerm;
import static it.unibz.inf.ontop.model.term.impl.GroundTermTools.isGroundTerm;

@Singleton
public class DatalogConversionTools {

    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final ImmutabilityTools immutabilityTools;
    private final CoreUtilsFactory coreUtilsFactory;
    private final TargetAtomFactory targetAtomFactory;

    @Inject
    private DatalogConversionTools(AtomFactory atomFactory, SubstitutionFactory substitutionFactory,
                                   ImmutabilityTools immutabilityTools,
                                   CoreUtilsFactory coreUtilsFactory, TargetAtomFactory targetAtomFactory) {
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.immutabilityTools = immutabilityTools;
        this.coreUtilsFactory = coreUtilsFactory;
        this.targetAtomFactory = targetAtomFactory;
    }

    /**
     * TODO: explain
     */
    public DataNode createDataNode(IntermediateQueryFactory iqFactory, DataAtom dataAtom,
                                          Collection<Predicate> tablePredicates) {

        if (tablePredicates.contains(dataAtom.getPredicate())) {
            return iqFactory.createExtensionalDataNode(dataAtom);
        }

        return iqFactory.createIntensionalDataNode(dataAtom);
    }


    /**
     * TODO: explain
     */
    public TargetAtom convertFromDatalogDataAtom(
            Function datalogDataAtom)
            throws DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException {

        Predicate datalogAtomPredicate = datalogDataAtom.getFunctionSymbol();
        if (!(datalogAtomPredicate instanceof AtomPredicate))
            throw new DatalogProgram2QueryConverterImpl.InvalidDatalogProgramException("The datalog predicate "
                    + datalogAtomPredicate + " is not an AtomPredicate!");

        AtomPredicate atomPredicate = (AtomPredicate) datalogAtomPredicate;

        ImmutableList.Builder<Variable> argListBuilder = ImmutableList.builder();
        ImmutableMap.Builder<Variable, ImmutableTerm> bindingBuilder = ImmutableMap.builder();

        /*
         * Replaces all the arguments by variables.
         * Makes sure the projected variables are unique.
         *
         * Creates allBindings entries if needed (in case of constant of a functional term)
         */
        VariableGenerator projectedVariableGenerator = coreUtilsFactory.createVariableGenerator(ImmutableSet.of());
        for (Term term : datalogDataAtom.getTerms()) {
            Variable newArgument;

            /*
             * If a projected variable occurs multiple times as an head argument,
             * rename it and keep track of the equivalence.
             *
             */
            if (term instanceof Variable) {
                Variable originalVariable = (Variable) term;
                newArgument = projectedVariableGenerator.generateNewVariableIfConflicting(originalVariable);
                if (!newArgument.equals(originalVariable)) {
                    bindingBuilder.put(newArgument, originalVariable);
                }
            }
            /*
             * Ground-term: replace by a variable and add a binding.
             * (easier to merge than putting the ground term in the data atom).
             */
            else if (isGroundTerm(term)) {
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, castIntoGroundTerm(term));
            }
            /*
             * Non-ground functional term
             */
            else {
                ImmutableTerm nonVariableTerm = immutabilityTools.convertIntoImmutableTerm(term);
                Variable newVariable = projectedVariableGenerator.generateNewVariable();
                newArgument = newVariable;
                bindingBuilder.put(newVariable, nonVariableTerm);
            }
            argListBuilder.add(newArgument);
        }

        DistinctVariableOnlyDataAtom dataAtom = atomFactory.getDistinctVariableOnlyDataAtom(atomPredicate, argListBuilder.build());
        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(bindingBuilder.build());


        return targetAtomFactory.getTargetAtom(dataAtom, substitution);
    }
}
