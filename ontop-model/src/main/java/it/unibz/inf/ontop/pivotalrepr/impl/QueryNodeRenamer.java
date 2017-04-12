package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;

import it.unibz.inf.ontop.model.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.pivotalrepr.transform.node.HomogeneousQueryNodeTransformer;

import java.util.Optional;
import java.util.stream.Collectors;

import static it.unibz.inf.ontop.model.impl.OntopModelSingletons.DATA_FACTORY;

/**
 * Renames query nodes according to one renaming substitution.
 */
public class QueryNodeRenamer implements HomogeneousQueryNodeTransformer {

    private final IntermediateQueryFactory iqFactory;
    private final InjectiveVar2VarSubstitution renamingSubstitution;

    public QueryNodeRenamer(IntermediateQueryFactory iqFactory, InjectiveVar2VarSubstitution renamingSubstitution) {
        this.iqFactory = iqFactory;
        this.renamingSubstitution = renamingSubstitution;
    }

    @Override
    public FilterNode transform(FilterNode filterNode) {
        return iqFactory.createFilterNode(renameBooleanExpression(filterNode.getFilterCondition()));
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return iqFactory.createExtensionalDataNode(renameDataAtom(extensionalDataNode.getProjectionAtom()));
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return iqFactory.createLeftJoinNode(renameOptionalBooleanExpression(
                leftJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public UnionNode transform(UnionNode unionNode){
        return iqFactory.createUnionNode(renameProjectedVars(unionNode.getVariables()));
//        return unionNode.clone();
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return iqFactory.createIntensionalDataNode(renameDataAtom(intensionalDataNode.getProjectionAtom()));
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode) {
        return iqFactory.createInnerJoinNode(renameOptionalBooleanExpression(innerJoinNode.getOptionalFilterCondition()));
    }

    @Override
    public ConstructionNode transform(ConstructionNode constructionNode) {
        return iqFactory.createConstructionNode(renameProjectedVars(constructionNode.getVariables()),
                renameSubstitution(constructionNode.getSubstitution()),
                renameOptionalModifiers(constructionNode.getOptionalModifiers())
                );
    }

    private ImmutableSet<Variable> renameProjectedVars(ImmutableSet<Variable> projectedVariables) {
        return projectedVariables.stream()
                .map(renamingSubstitution::applyToVariable)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
    }

    @Override
    public EmptyNode transform(EmptyNode emptyNode) {
        return emptyNode.clone();
    }

    public TrueNode transform(TrueNode trueNode) {
        return trueNode.clone();
    }

    private Optional<ImmutableQueryModifiers> renameOptionalModifiers(Optional<ImmutableQueryModifiers> optionalModifiers) {
        if (optionalModifiers.isPresent()) {
            return renamingSubstitution.applyToQueryModifiers(optionalModifiers.get());
        }
        else {
            return Optional.empty();
        }
    }

    private ImmutableExpression renameBooleanExpression(ImmutableExpression booleanExpression) {
        return renamingSubstitution.applyToBooleanExpression(booleanExpression);
    }


    private DataAtom renameDataAtom(DataAtom atom) {
        ImmutableList.Builder<VariableOrGroundTerm> argListBuilder = ImmutableList.builder();
        for (VariableOrGroundTerm term : atom.getArguments()) {
            argListBuilder.add(renamingSubstitution.applyToTerm(term));
        }
        return DATA_FACTORY.getDataAtom(atom.getPredicate(), argListBuilder.build());
    }

    private Optional<ImmutableExpression> renameOptionalBooleanExpression(
            Optional<ImmutableExpression> optionalExpression) {
        if (!optionalExpression.isPresent())
            return Optional.empty();

        ImmutableExpression expression = optionalExpression.get();
        return Optional.of(renameBooleanExpression(expression));
    }

    private ImmutableSubstitution<ImmutableTerm> renameSubstitution(ImmutableSubstitution<ImmutableTerm> substitution) {
        return renamingSubstitution.applyRenaming(substitution);
    }
}